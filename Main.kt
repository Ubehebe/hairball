package jvmutil.deps

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

fun main(args: Array<String>) {
  val flags = Flags()
  JCommander.newBuilder().addObject(flags).build().parse(*args)
  val graph: Graph<JavaClass, DefaultEdge> = System.`in`.readJdepsClassGraph()
  flags.mode().run(graph)
}

class Flags {
  @Parameter(names = ["--mode"]) private var programMode = "ProposeClusters"

  @Parameter(names = ["-n"]) private var numClusters = -1

  fun mode(): ProgramMode =
      when (programMode) {
        "ProposeClusters" -> ProposeClusters(numClusters)
        "CountSccs" -> CountSccs
        else -> error("unknown mode: $programMode")
      }
}
