package jvmutil.deps

import com.google.common.collect.HashMultimap
import com.google.common.graph.EndpointPair
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph

data class Cluster<T : DotNode>(val name: String, val nodes: Set<T>) : DotNode {
  override fun name(): String = name

  override fun label(): String =
      nodes
          .map { it.label() }
          .sorted()
          .joinToString(separator = "\\n", prefix = "\"", postfix = "\"")
}

fun <T : DotNode> spectralClustering(
    graph: ImmutableGraph<NodeWithIndex<T>>,
    numClusters: Int
): ImmutableGraph<Cluster<T>> {
  val pb = ProcessBuilder("jvmutil/deps/spectral_clustering", "--n_clusters=$numClusters")
  val clusters: List<String> =
      pb.start().let {
        it.outputStream.write(graph.toAdjacencyMatrix().toByteArray())
        it.outputStream.close()
        val status = it.waitFor()
        check(status == 0) {
          "spectral_clustering exited with status $status: ${String(it.errorStream.readAllBytes())}"
        }
        it.inputStream.reader().readLines()
      }
  return graph.clustered(clusters)
}

private fun <T> ImmutableGraph<NodeWithIndex<T>>.toAdjacencyMatrix(): String {
  val s = StringBuilder()
  for (u in nodes()) {
    for (v in nodes()) {
      s.append(if (v in successors(u)) "1 " else "0 ")
    }
    s.append("\n")
  }
  return s.toString()
}

private fun <T : DotNode> ImmutableGraph<NodeWithIndex<T>>.clustered(
    clusterNames: List<String>
): ImmutableGraph<Cluster<T>> {
  check(clusterNames.size == nodes().size) {
    "expected ${nodes().size} clusters, got ${clusterNames.size}"
  }

  val nodeIndexesToClusterNames: Map<Int, String> =
      clusterNames
          .mapIndexed { nodeIndex, clusterName -> nodeIndex to "cluster_$clusterName" }
          .toMap()

  val clusterSet = HashMultimap.create<String, T>()

  nodes().forEach {
    val clusterName = nodeIndexesToClusterNames[it.index]
    clusterSet.put(clusterName, it.node)
  }

  val clusters: Map<String, Cluster<T>> =
      clusterSet
          .asMap()
          .map { (name, nodes) -> Cluster(name, nodes.toSet()) }
          .associateBy { it.name }

  val g: MutableGraph<Cluster<T>> =
      if (isDirected) GraphBuilder.directed().build() else GraphBuilder.undirected().build()
  edges()
      .map {
        val uCluster = clusters[nodeIndexesToClusterNames[it.nodeU().index]]
        val vCluster = clusters[nodeIndexesToClusterNames[it.nodeV().index]]
        EndpointPair.ordered(uCluster, vCluster)
      }
      .filter { it.nodeU() != it.nodeV() }
      .forEach { g.putEdge(it) }
  return ImmutableGraph.copyOf(g)
}
