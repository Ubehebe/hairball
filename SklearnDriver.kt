package jvmutil.deps

import com.google.common.collect.HashMultimap
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph

fun <T : DotNode> spectralClustering(
    graph: ImmutableGraph<NodeWithIndex<T>>,
    numClusters: Int
): ClusteredGraph<T> {
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
    clusters: List<String>
): ClusteredGraph<T> {
  check(clusters.size == nodes().size) { "expected ${nodes().size} clusters, got ${clusters.size}" }

  val nodeIndexesToClusterNames: Map<Int, String> =
      clusters.mapIndexed { nodeIndex, clusterName -> nodeIndex to "cluster_$clusterName" }.toMap()

  val clusterSet = HashMultimap.create<String, T>()

  nodes().forEach {
    val clusterName = nodeIndexesToClusterNames[it.index]
    clusterSet.put(clusterName, it.node)
  }

  val c = clusterSet.asMap().map { (name, nodes) -> Cluster(name, nodes.toSet()) }.toSet()

  val g: MutableGraph<T> =
      if (isDirected) GraphBuilder.directed().build() else GraphBuilder.undirected().build()
  edges().forEach { g.putEdge(it.nodeU().node, it.nodeV().node) }
  return ClusteredGraph(ImmutableGraph.copyOf(g), c)
}
