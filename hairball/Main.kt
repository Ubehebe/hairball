package hairball

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.system.exitProcess
import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge

fun main(args: Array<String>) {
  val command = args.parse()

  // read the class dependency graph from stdin.
  val graph: Graph<JavaClass, DefaultEdge> = System.`in`.readJdepsClassGraph()

  // condense the graph into its strongly connected components. each scc in the original graph
  // becomes a single vertex in the new graph.
  val sccs: Graph<Set<JavaClass>, DefaultEdge> =
      KosarajuStrongConnectivityInspector(graph).condense()

  val biggestCluster = sccs.vertexSet().maxBy { it.size }
  log.info {
    """condensed ${graph.vertexSet().size} classes into ${sccs.vertexSet().size} strongly
connected components. the largest component contains ${biggestCluster.size} classes that
transitively depend on one another. you will have to break these dependencies manually."""
        .split("\n")
        .joinToString(" ")
  }

  // run the program on the condensed graph.
  command.run(sccs)
}

private fun Array<String>.parse(): Command {
  val jc =
      JCommander.newBuilder()
          .programName("hairball")
          .addCommand(ProposeClusters)
          .addCommand(MinimalClusters)
          .addObject(TopLevelFlags)
          .build()
  jc.parse(*this)
  return when (jc.parsedCommand) {
    "ProposeClusters" -> ProposeClusters
    "MinimalClusters" -> MinimalClusters
    else -> {
      jc.usage()
      exitProcess(0)
    }
  }
}

private object TopLevelFlags {
  @Parameter(names = ["-h", "--help"]) var help = false
}

sealed interface Command {
  fun run(stronglyConnectedComponents: Graph<Set<JavaClass>, DefaultEdge>)
}

private val log = KotlinLogging.logger {}
