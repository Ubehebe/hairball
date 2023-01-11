package hairball

import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge

/**
 * Various views of the dependency graph. This is the input to [Command.run], so different commands
 * can operate on the view that makes sense to them.
 */
data class DependencyGraphs(
    /** The raw class->class graph. */
    val classGraph: Graph<JavaClass, DefaultEdge>,
    /** The strongly connected components. Each vertex is a subgraph of [classGraph]. */
    val stronglyConnectedComponents: Graph<Graph<JavaClass, DefaultEdge>, DefaultEdge>,
    /**
     * Like [stronglyConnectedComponents], but each vertex is a set of nodes in the original graph.
     * This is a bit simpler to work with.
     */
    val simplifiedComponents: Graph<Set<JavaClass>, DefaultEdge>,
)

fun Graph<JavaClass, DefaultEdge>.variants(): DependencyGraphs {
  // condense the graph into its strongly connected components. each scc in the original graph
  // becomes a single vertex in the new graph.
  val stronglyConnectedComponents: Graph<Graph<JavaClass, DefaultEdge>, DefaultEdge> =
      KosarajuStrongConnectivityInspector(this).condensation
  val simplifiedComponents = stronglyConnectedComponents.simplified()

  val biggestCluster = simplifiedComponents.vertexSet().maxBy { it.size }
  log.info {
    """condensed ${vertexSet().size} classes into ${simplifiedComponents.vertexSet().size} strongly
connected components. the largest component contains ${biggestCluster.size} classes that
transitively depend on one another. you will have to break these dependencies manually."""
        .split("\n")
        .joinToString(" ")
  }

  return DependencyGraphs(classGraph = this, stronglyConnectedComponents, simplifiedComponents)
}

private val log = KotlinLogging.logger {}
