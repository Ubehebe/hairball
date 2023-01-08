package jvmutil.deps

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.dot.DOTExporter

fun main(args: Array<String>) {
  // read in the graph of class -> class dependencies
  val graph: Graph<JavaClass, DefaultEdge> = System.`in`.readJdepsClassGraph()

  // condense the graph into its strongly connected components. each scc in the original graph
  // becomes a single vertex in the new graph.
  val condensed: Graph<ClassCluster, DefaultEdge> =
      KosarajuStrongConnectivityInspector(graph).condensation

  DOTExporter<ClassCluster, DefaultEdge>().apply {
    setVertexAttributeProvider { it.dotAttrs() }
    exportGraph(condensed, System.out)
  }
}

typealias ClassCluster = Graph<JavaClass, DefaultEdge>

private fun ClassCluster.dotAttrs(): Map<String, Attribute> =
    listOf(
            "shape" to "box",
            "label" to vertexSet().map { it.label() }.sorted().joinToString(separator = "\\n"))
        .associate { it.asDotAttr() }

private fun Pair<String, String>.asDotAttr(): Pair<String, Attribute> =
    first to
        object : Attribute {
          override fun getType(): AttributeType = AttributeType.STRING
          override fun getValue(): String = second
        }
