package jvmutil.deps

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.HashMultimap
import com.google.common.graph.EndpointPair
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph
import mu.KotlinLogging

fun main(args: Array<String>) {
  val flags = Flags()
  JCommander.newBuilder().addObject(flags).build().parse(*args)

  val builder = GraphBuilder.directed().build<Package>()
  System.`in`.reader().useLines { lines ->
    lines.mapNotNull { parseJdepsLine(it) }.forEach { (from, to) -> builder.putEdge(from, to) }
  }
  val graph = ImmutableGraph.copyOf(builder).indexed()

  val pb = ProcessBuilder("jvmutil/deps/spectral_clustering", "--n_clusters=${flags.numClusters}")
  val clusters: List<String> =
      pb.start().let {
        it.outputStream.write(graph.toAdjacencyMatrix().toByteArray())
        it.outputStream.close()
        val status = it.waitFor()
        check(status == 0) {
          "spectral_clustering exited with status $status: ${String(it.errorStream.readAllBytes())}"
        }
        it.inputStream.reader().readLines()
      }
  println(graph.clustered(clusters).toDot())
}

@Parameters(separators = "= ")
private class Flags {
  @Parameter(names = ["-n"], required = true) var numClusters = -1
}

private val splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings()

private fun parseJdepsLine(line: String): Pair<Package, Package>? {
  val parts = splitter.splitToList(line)
  // fully.qualified.From -> fully.qualified.To jar-name.jar
  return when {
    parts.size < 4 || parts[1] != "->" -> {
      log.warn { "malformed line, skipping: $parts" }
      null
    }
    else -> Package(parts[0]) to Package(parts[2])
  }
}

data class Package(val fullyQualified: String) : Comparable<Package>, DotNode {

  override fun toString(): String = "\"$fullyQualified\""

  override fun label(): String {
    val parts = Splitter.on('.').splitToList(fullyQualified)
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  override fun compareTo(other: Package): Int = fullyQualified.compareTo(other.fullyQualified)
}

private interface DotNode {
  fun label(): String
}

private data class NodeWithCluster<T>(val node: T, val cluster: String)

private fun <T : DotNode> ImmutableGraph<NodeWithIndex<T>>.clustered(
    clusters: List<String>
): ClusteredGraph<T> {
  check(clusters.size == nodes().size) { "expected ${nodes().size} clusters, got ${clusters.size}" }

  val nodeIndexesToClusterNames: Map<Int, String> =
      clusters.mapIndexed { nodeIndex, clusterName -> nodeIndex to "cluster_$clusterName" }.toMap()

  val clusterSet = HashMultimap.create<String, T>()

  nodes().forEach {
    val clusterName = nodeIndexesToClusterNames[it.index]
    clusterSet.put(clusterName, it.node)
  }

  val c = clusterSet.asMap().map { (name, nodes) -> Cluster(name, nodes.toSet()) }.toSet()

  val g: MutableGraph<T> =
      if (isDirected) GraphBuilder.directed().build() else GraphBuilder.undirected().build()
  edges().forEach { g.putEdge(it.nodeU().node, it.nodeV().node) }
  return ClusteredGraph(ImmutableGraph.copyOf(g), c)
}

private fun <T> ImmutableGraph<T>.indexed(): ImmutableGraph<NodeWithIndex<T>> {
  val nodesToIndexes: Map<T, NodeWithIndex<T>> =
      nodes().mapIndexed { i, node -> node to NodeWithIndex(node, i) }.toMap()
  val g: MutableGraph<NodeWithIndex<T>> =
      if (isDirected) GraphBuilder.directed().build() else GraphBuilder.undirected().build()
  edges().forEach { g.putEdge(nodesToIndexes[it.nodeU()]!!, nodesToIndexes[it.nodeV()]!!) }
  return ImmutableGraph.copyOf(g)
}

private fun <T> ImmutableGraph<NodeWithIndex<T>>.toAdjacencyMatrix(): String {
  val s = StringBuilder()
  for (u in nodes()) {
    for (v in nodes()) {
      s.append(if (v in successors(u)) "1 " else "0 ")
    }
    s.append("\n")
  }
  return s.toString()
}

private data class NodeWithIndex<T>(val node: T, val index: Int)

data class Cluster<T>(val name: String, val nodes: Set<T>)

private data class ClusteredGraph<T : DotNode>(
    private val graph: ImmutableGraph<T>,
    private val clusters: Set<Cluster<T>>
) {

  private val nodesToClusters: Map<T, Cluster<T>>
  init {
    val m = mutableMapOf<T, Cluster<T>>()
    clusters.forEach { c -> c.nodes.forEach { u -> m[u] = c } }
    nodesToClusters = m
  }

  fun toDot(): String {
    val subgraphDecls: String =
        clusters
            .sortedBy { it.name }
            .joinToString(separator = "\n") { (clusterName, nodes) ->
              nodes.joinToString(
                  separator = "\n",
                  prefix = "  subgraph $clusterName {\n    color=red;\n",
                  postfix = "\n  }") {
                    "    $it [label=\"${it.label()}\"];"
                  }
            }

    val edges =
        graph
            .edges()
            .filter { it.goesBetweenClusters() }
            .map { it.toDotEdge() }
            .sorted()
            .joinToString(separator = "\n")
    return "digraph G {\ncompound=true;\noutputorder=\"edgesfirst\";\n$subgraphDecls\n$edges\n}"
  }

  private fun EndpointPair<T>.goesBetweenClusters(): Boolean =
      nodesToClusters[nodeU()] != nodesToClusters[nodeV()]

  private fun EndpointPair<T>.toDotEdge(): String {
    return "  ${nodeU()} -> ${nodeV()} [color=lightgray ltail=\"${nodeU().clusterName()}\" lhead=\"${nodeV().clusterName()}\"];"
  }

  private fun T.clusterName(): String = nodesToClusters[this]!!.name
}

private val log = KotlinLogging.logger {}
