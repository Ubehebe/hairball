package jvmutil.deps

import com.google.common.graph.EndpointPair
import com.google.common.graph.ImmutableGraph

interface DotNode {
  fun name(): String
  fun label(): String

  fun asDotNodeDecl(): String = "  ${name()} [label=${label()}];"
}

fun <T : DotNode> ImmutableGraph<T>.toDot(): String {
  val nodes = nodes().map { it.asDotNodeDecl() }.sorted().joinToString(separator = "\n")
  val edges = edges().map { it.asDotEdgeDecl() }.sorted().joinToString(separator = "\n")
  return "digraph G {\n  node [shape=box];\n$nodes\n$edges\n}"
}

private fun <T : DotNode> EndpointPair<T>.asDotEdgeDecl(): String =
    "  ${nodeU().name()} -> ${nodeV().name()};"
