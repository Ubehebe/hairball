package jvmutil.deps

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph

fun main(args: Array<String>) {
  val flags = Flags()
  JCommander.newBuilder().addObject(flags).build().parse(*args)

  val builder = GraphBuilder.directed().build<Package>()
  System.`in`.reader().useLines { lines ->
    lines.mapNotNull { parseJdepsLine(it) }.forEach { (from, to) -> builder.putEdge(from, to) }
  }
  val graph = ImmutableGraph.copyOf(builder).indexed()

  val clusteredGraph = spectralClustering(graph, numClusters = flags.numClusters)
  println(clusteredGraph.toDot())
}

@Parameters(separators = "= ")
private class Flags {
  @Parameter(names = ["-n"], required = true) var numClusters = -1
}
