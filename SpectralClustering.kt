package jvmutil.deps

import com.google.common.collect.HashMultimap
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ClusteringAlgorithm
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

/**
 * [ClusteringAlgorithm] implementation that delegates to scikit-learn's spectral_clustering method
 * (https://scikit-learn.org/stable/modules/generated/sklearn.cluster.spectral_clustering.html).
 *
 * jgrapht ships with three ClusteringAlgorithm impls, but only
 * [org.jgrapht.alg.clustering.GirvanNewmanClustering] works with directed graphs. It takes
 * ~quadratic time, which is infeasible for large graphs. Spectral clustering seems to be faster
 * (TODO: characterize performance).
 *
 * TODO: try to use a jvm implementation like
 * https://haifengl.github.io/clustering.html#spectral-clustering.
 */
class SpectralClustering<V>(
    private val graph: Graph<V, DefaultEdge>,
    private val numClusters: Int
) : ClusteringAlgorithm<V> {
  override fun getClustering(): ClusteringAlgorithm.Clustering<V> {

    val indexed: Graph<IndexedVertex<V>, DefaultEdge> = graph.indexed()

    val pb = ProcessBuilder("jvmutil/deps/spectral_clustering", "--n_clusters=$numClusters")
    val clusterNames: List<String> =
        pb.start().let {
          it.outputStream.write(indexed.toAdjacencyMatrix().toByteArray())
          it.outputStream.close()
          val status = it.waitFor()
          check(status == 0) {
            "spectral_clustering exited with status $status: ${String(it.errorStream.readAllBytes())}"
          }
          it.inputStream.reader().readLines()
        }
    return ClusteringImpl(indexed.clustered(clusterNames).vertexSet())
  }
}

private fun <T> Graph<IndexedVertex<T>, DefaultEdge>.toAdjacencyMatrix(): String {
  val s = StringBuilder()
  for (u in vertexSet()) {
    for (v in vertexSet()) {
      s.append(if (containsEdge(u, v)) "1 " else "0 ")
    }
    s.append("\n")
  }
  return s.toString()
}

private inline fun <V, reified E> Graph<IndexedVertex<V>, E>.clustered(
    clusterNames: List<String>
): Graph<Cluster<V>, E> {
  check(clusterNames.size == vertexSet().size) {
    "expected ${vertexSet().size} clusters, got ${clusterNames.size}"
  }

  val nodeIndexesToClusterNames: Map<Int, String> =
      clusterNames
          .mapIndexed { nodeIndex, clusterName -> nodeIndex to "cluster_$clusterName" }
          .toMap()

  val clusterSet = HashMultimap.create<String, V>()

  vertexSet().forEach {
    val clusterName = nodeIndexesToClusterNames[it.index]
    clusterSet.put(clusterName, it.vertex)
  }

  val clusters: Map<String, Cluster<V>> =
      clusterSet
          .asMap()
          .map { (name, nodes) -> Cluster(name, nodes.toSet()) }
          .associateBy { it.name }

  val g = GraphTypeBuilder.directed<Cluster<V>, E>().edgeClass(E::class.java).buildGraph()
  clusters.values.forEach { g.addVertex(it) }
  edgeSet().forEach {
    val uCluster = clusters[nodeIndexesToClusterNames[getEdgeSource(it).index]]
    val vCluster = clusters[nodeIndexesToClusterNames[getEdgeTarget(it).index]]
    if (uCluster != vCluster) {
      g.addEdge(uCluster, vCluster)
    }
  }
  return AsUnmodifiableGraph(g)
}

private inline fun <V, reified E> Graph<V, E>.indexed(): Graph<IndexedVertex<V>, E> {
  val verticesToIndexes: Map<V, IndexedVertex<V>> =
      vertexSet().mapIndexed { i, v -> v to IndexedVertex(v, i) }.toMap()

  val g = GraphTypeBuilder.directed<IndexedVertex<V>, E>().edgeClass(E::class.java).buildGraph()
  verticesToIndexes.values.forEach { g.addVertex(it) }
  edgeSet().forEach {
    val u = verticesToIndexes[getEdgeSource(it)]
    val v = verticesToIndexes[getEdgeTarget(it)]
    g.addEdge(u, v)
  }

  return AsUnmodifiableGraph(g)
}

private data class IndexedVertex<V>(val vertex: V, val index: Int)
