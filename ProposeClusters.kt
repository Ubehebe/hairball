package jvmutil.deps

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.dot.DOTExporter

class ProposeClusters(
    private val numClusters: Int,
    private val assignLabels: AssignLabels,
) : ProgramMode {

  init {
    check(numClusters > 1) { "invalid num clusters: $numClusters" }
  }

  override fun run(graph: Graph<JavaClass, DefaultEdge>) {
    // condense the graph into its strongly connected components. each scc in the original graph
    // becomes a single vertex in the new graph.
    val sccs: Graph<Set<JavaClass>, DefaultEdge> =
        KosarajuStrongConnectivityInspector(graph).condense()

    check(numClusters <= sccs.vertexSet().size) {
      "requested $numClusters clusters, but there are only ${sccs.vertexSet().size} strongly connected components."
    }

    // cluster the sccs into the given number of clusters, shelling out to scikit-learn.
    // TODO: ideally, we could perform the clustering purely on the jvm. jgrapht ships with three
    // ClusteringAlgorithm impls, but only org.jgrapht.alg.clustering.GirvanNewmanClustering works
    // with directed graphs, and it's unacceptably slow.
    // https://haifengl.github.io/clustering.html#spectral-clustering is a java impl of spectral
    // clustering, but it gives qualitatively worse results than scikit-learn, for example lots of
    // singleton clusters and one huge residual cluster.
    val clustered: Graph<Set<Set<JavaClass>>, DefaultEdge> =
        sccs.condense(SklearnSpectralClustering(graph = sccs, numClusters, assignLabels).clustering)

    // the clustering could have produced new circular deps, which is not helpful. re-condense the
    // graph into strongly connected components again to get rid of them.
    // TODO: get the generic types under control
    val reCondensed: Graph<Set<Set<Set<JavaClass>>>, DefaultEdge> =
        KosarajuStrongConnectivityInspector(clustered).condense()

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
