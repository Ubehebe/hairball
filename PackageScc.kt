package jvmutil.deps

import com.google.common.graph.EndpointPair
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.guava.ImmutableGraphAdapter
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.dot.DOTExporter

fun main(args: Array<String>) {
  val graph = readJdepsGraphFromStdin()
  val jgraphTGraph = ImmutableGraphAdapter(graph)
  val condensed = KosarajuStrongConnectivityInspector(jgraphTGraph).condensation
  DOTExporter<Graph<JavaPackage, EndpointPair<JavaPackage>>, DefaultEdge>().apply {
    setVertexAttributeProvider { condensedVertex ->
      listOf(
              "shape" to "box",
              "label" to
                  condensedVertex
                      .vertexSet()
                      .map { it.label() }
                      .sorted()
                      .joinToString(separator = "\\n"))
          .associate { it.asAttr() }
    }
    exportGraph(condensed, System.out)
  }
}

private fun Pair<String, String>.asAttr(): Pair<String, Attribute> =
    first to
        object : Attribute {
          override fun getType(): AttributeType = AttributeType.STRING
          override fun getValue(): String = second
        }
