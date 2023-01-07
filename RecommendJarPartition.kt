package jvmutil.deps

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

fun main(args: Array<String>) {
  val flags = Flags()
  JCommander.newBuilder().addObject(flags).build().parse(*args)

  val graph = readJdepsGraphFromStdin().indexed()

  val clusteredGraph = spectralClustering(graph, numClusters = flags.numClusters)
  println(clusteredGraph.toDot())
}

@Parameters(separators = "= ")
private class Flags {
  @Parameter(names = ["-n"], required = true) var numClusters = -1
}
