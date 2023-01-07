package jvmutil.deps

import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.dot.DOTExporter

fun main(args: Array<String>) {
  val graph = System.`in`.readJdepsClassGraph()
  val condensed = KosarajuStrongConnectivityInspector(graph).condensation
  DOTExporter<Graph<JavaClass, DefaultEdge>, DefaultEdge>().apply {
    setVertexAttributeProvider { condensedVertex ->
      listOf(
              "shape" to "box",
              "label" to
                  condensedVertex
                      .vertexSet()
                      .map { it.label() }
                      .sorted()
                      .joinToString(separator = "\\n"))
          .associate { it.asDotAttr() }
    }
    exportGraph(condensed, System.out)
  }
}
