package jvmutil.deps

import com.google.common.graph.EndpointPair
import com.google.common.graph.ImmutableGraph

data class ClusteredGraph<T : DotNode>(
    private val graph: ImmutableGraph<T>,
    private val clusters: Set<Cluster<T>>
) {

  private val nodesToClusters: Map<T, Cluster<T>>
  init {
    val m = mutableMapOf<T, Cluster<T>>()
    clusters.forEach { c -> c.nodes.forEach { u -> m[u] = c } }
    nodesToClusters = m
  }

  fun toDot(): String {
    val subgraphDecls: String =
        clusters
            .sortedBy { it.name }
            .joinToString(separator = "\n") { (clusterName, nodes) ->
              nodes.joinToString(
                  separator = "\n",
                  prefix = "  subgraph $clusterName {\n    color=red;\n",
                  postfix = "\n  }") {
                    "    $it [label=\"${it.label()}\"];"
                  }
            }

    val edges =
        graph
            .edges()
            .filter { it.goesBetweenClusters() }
            .map { it.toDotEdge() }
            .sorted()
            .joinToString(separator = "\n")
    return "digraph G {\ncompound=true;\noutputorder=\"edgesfirst\";\n$subgraphDecls\n$edges\n}"
  }

  private fun EndpointPair<T>.goesBetweenClusters(): Boolean =
      nodesToClusters[nodeU()] != nodesToClusters[nodeV()]

  private fun EndpointPair<T>.toDotEdge(): String {
    return "  ${nodeU()} -> ${nodeV()} [color=lightgray ltail=\"${nodeU().clusterName()}\" lhead=\"${nodeV().clusterName()}\"];"
  }

  private fun T.clusterName(): String = nodesToClusters[this]!!.name
}

data class Cluster<T>(val name: String, val nodes: Set<T>)
