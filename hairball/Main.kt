package hairball

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.system.exitProcess
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

fun main(args: Array<String>) {
  args.parse().apply {
    val graph: Graph<JavaClass, DefaultEdge> = System.`in`.readJdepsClassGraph()
    run(graph.variants())
  }
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
  fun run(graphs: DependencyGraphs)
}
