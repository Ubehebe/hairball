package hairball

import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ClusteringAlgorithm
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.builder.GraphTypeBuilder

/**
 * Returns a graph whose vertices are the clusters of the original graph according to [clustering],
 * and whose edges correspond to the edges in the original graph that go between different clusters.
 */
inline fun <V, reified E> Graph<V, E>.condense(
    clustering: ClusteringAlgorithm.Clustering<V>
): Graph<Set<V>, E> {
  val graph = GraphTypeBuilder.directed<Set<V>, E>().edgeClass(E::class.java).buildGraph()
  val clusters: List<Cluster<V>> =
      clustering.clusters.mapIndexed { i, nodes -> Cluster("cluster$i", nodes) }
  val verticesToClusters: Map<V, Cluster<V>> =
      clusters.flatMap { c -> c.vertices.map { it to c } }.toMap()

  clusters.map { it.vertices }.forEach { graph.addVertex(it) }
  edgeSet().forEach {
    val uCluster: Set<V> = verticesToClusters[getEdgeSource(it)]!!.vertices
    val vCluster = verticesToClusters[getEdgeTarget(it)]!!.vertices
    if (uCluster != vCluster) {
      graph.addEdge(uCluster, vCluster)
    }
  }

  return AsUnmodifiableGraph(graph)
}

/**
 * [StrongConnectivityAlgorithm.getCondensation] returns a graph with vertices of type Graph<V,E>.
 * This method wraps that to return a graph with vertices of type Set<V>, which I find more
 * intuitive.
 */
inline fun <V, reified E> Graph<Graph<V, E>, E>.simplified(): Graph<Set<V>, E> {
  val g = GraphTypeBuilder.directed<Set<V>, E>().edgeClass(E::class.java).buildGraph()

  vertexSet().map { it.vertexSet().toSet() }.forEach { g.addVertex(it) }
  edgeSet().forEach {
    val u = getEdgeSource(it).vertexSet().toSet()
    val v = getEdgeTarget(it).vertexSet().toSet()
    if (u != v) {
      g.addEdge(u, v)
    }
  }

  return AsUnmodifiableGraph(g)
}
