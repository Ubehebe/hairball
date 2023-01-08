package jvmutil.deps

import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering

data class Cluster<T>(val name: String, val vertices: Set<T>)

/** Adapts a set of [Cluster]s into a [Clustering]. */
class ClusteringImpl<T>(private val clusters: Set<Cluster<T>>) : Clustering<T> {
  override fun iterator(): MutableIterator<MutableSet<T>> =
      clusters.map { it.vertices.toMutableSet() }.toMutableSet().iterator()

  override fun getNumberClusters(): Int = clusters.size

  override fun getClusters(): MutableList<MutableSet<T>> =
      clusters.map { it.vertices.toMutableSet() }.toMutableList()
}
