package hairball

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.dot.DOTExporter

@Parameters(
    commandNames = ["ProposeClusters"],
    commandDescription =
        "attempt to break input jar into n acyclic clusters. writes a .dot graph of the proposed clusters to stdout.",
    separators = "= ",
)
object ProposeClusters : Command {
  @Parameter(
      names = ["--n-clusters"],
      description =
          "target number of clusters to create. the actual number of clusters may be smaller; see README.md for discussion.",
      required = true,
  )
  private lateinit var nClusters: Integer

  @Parameter(
      names = ["--assign-labels"],
      description =
          """pass custom assign_labels arg to sklearn.spectral_clustering. See https://scikit-learn.org/stable/modules/generated/sklearn.cluster.spectral_clustering.html.""")
  private var assignLabels = AssignLabels.kmeans

  override fun run(stronglyConnectedComponents: Graph<Set<JavaClass>, DefaultEdge>) {
    val targetClusters = nClusters.toInt()
    check(targetClusters > 1) { "got --n-clusters $nClusters, need > 1" }
    check(targetClusters <= stronglyConnectedComponents.vertexSet().size) {
      "requested $targetClusters clusters, but there are only ${stronglyConnectedComponents.vertexSet().size} strongly connected components."
    }

    // cluster the sccs into the given number of clusters, shelling out to scikit-learn.
    // TODO: ideally, we could perform the clustering purely on the jvm. jgrapht ships with three
    // ClusteringAlgorithm impls, but only org.jgrapht.alg.clustering.GirvanNewmanClustering works
    // with directed graphs, and it's unacceptably slow.
    // https://haifengl.github.io/clustering.html#spectral-clustering is a java impl of spectral
    // clustering, but it gives qualitatively worse results than scikit-learn, for example lots of
    // singleton clusters and one huge residual cluster.
    val clustered: Graph<Set<Set<JavaClass>>, DefaultEdge> =
        stronglyConnectedComponents.condense(
            SklearnSpectralClustering(stronglyConnectedComponents, targetClusters, assignLabels)
                .clustering)

    // the clustering could have produced new circular deps, which is not helpful. re-condense the
    // graph into strongly connected components again to get rid of them.
    // TODO: get the generic types under control
    val reCondensed: Graph<Set<Set<Set<JavaClass>>>, DefaultEdge> =
        KosarajuStrongConnectivityInspector(clustered).condense()

    val actualClusters = reCondensed.vertexSet().size
    if (actualClusters < targetClusters) {
      log.info {
        "requested $targetClusters clusters, but there are only $actualClusters in the output due to merging of cyclic deps. Try increasing --n-clusters."
      }
    }

    DOTExporter<Set<Set<Set<JavaClass>>>, DefaultEdge>().apply {
      setVertexAttributeProvider { it.dotAttrs() }
      exportGraph(reCondensed, System.out)
    }
  }
}

fun Pair<String, String>.asDotAttr(): Pair<String, Attribute> =
    first to
        object : Attribute {
          override fun getType(): AttributeType = AttributeType.STRING
          override fun getValue(): String = second
        }

private fun Set<Set<Set<JavaClass>>>.dotAttrs(): Map<String, Attribute> {
  val allClasses = flatMap { it.flatten() }
  return listOf(
          "shape" to "box",
          "label" to allClasses.map { it.label() }.sorted().joinToString(separator = "\\n"))
      .associate { it.asDotAttr() }
}

private val log = KotlinLogging.logger {}
