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

  @Parameter(names = ["-n"]) private var numClusters: Int? = null

  fun mode(): ProgramMode =
      when (programMode) {
        "ProposeClusters" -> numClusters?.let { ProposeClusters(it) }
                ?: error("-n is required for --mode ProposeClusters")
        "MinimalClusters" -> {
          check(numClusters == null) { "-n must not be set for --mode MinimalClusters" }
          MinimalClusters
        }
        else -> error("unknown mode: $programMode")
      }
}

sealed interface ProgramMode {
  fun run(graph: Graph<JavaClass, DefaultEdge>)
}
