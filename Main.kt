package jvmutil.deps

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.dot.DOTExporter

fun main(args: Array<String>) {
  val flags = Flags()
  JCommander.newBuilder().addObject(flags).build().parse(*args)

  // read in the graph of class -> class dependencies
  val graph: Graph<JavaClass, DefaultEdge> = System.`in`.readJdepsClassGraph()

  // condense the graph into its strongly connected components. each scc in the original graph
  // becomes a single vertex in the new graph.
  val sccs: Graph<Set<JavaClass>, DefaultEdge> =
      KosarajuStrongConnectivityInspector(graph).condense()

  check(flags.numClusters <= sccs.vertexSet().size) {
    "requested ${flags.numClusters} clusters, but there are only ${sccs.vertexSet().size} strongly connected components."
  }

  // cluster the sccs into the given number of clusters.
  val clustered: Graph<Set<Set<JavaClass>>, DefaultEdge> =
      sccs.condense(SpectralClustering(sccs, flags.numClusters).clustering)

  // the clustering could have produced new mutual imports, which is not helpful. condense the graph
  // into strongly connected components again to get rid of them.
  // TODO: get the generic types under control
  // TODO: i need better intuition about how a graph partition can change strongly connected
  // components. (ideally, we could partition while preserving the sccs.)
  val reCondensed: Graph<Set<Set<Set<JavaClass>>>, DefaultEdge> =
      KosarajuStrongConnectivityInspector(clustered).condense()

  DOTExporter<Set<Set<Set<JavaClass>>>, DefaultEdge>().apply {
    setVertexAttributeProvider { it.dotAttrs() }
    exportGraph(reCondensed, System.out)
  }
}

class Flags {
  @Parameter(names = ["-n"], required = true) var numClusters = -1
}

private fun Pair<String, String>.asDotAttr(): Pair<String, Attribute> =
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
