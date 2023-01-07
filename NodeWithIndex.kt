package jvmutil.deps

import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph

data class NodeWithIndex<T>(val node: T, val index: Int)

fun <T> ImmutableGraph<T>.indexed(): ImmutableGraph<NodeWithIndex<T>> {
  val nodesToIndexes: Map<T, NodeWithIndex<T>> =
      nodes().mapIndexed { i, node -> node to NodeWithIndex(node, i) }.toMap()
  val g: MutableGraph<NodeWithIndex<T>> =
      if (isDirected) GraphBuilder.directed().build() else GraphBuilder.undirected().build()
  edges().forEach { g.putEdge(nodesToIndexes[it.nodeU()]!!, nodesToIndexes[it.nodeV()]!!) }
  return ImmutableGraph.copyOf(g)
}
