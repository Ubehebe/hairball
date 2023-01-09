package jvmutil.deps

import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.dot.DOTExporter

object MinimalClusters : ProgramMode {
  override fun run(graph: Graph<JavaClass, DefaultEdge>) {
    // condense the graph into its strongly connected components. each scc in the original graph
    // becomes a single vertex in the new graph.
    val sccs: Graph<Set<JavaClass>, DefaultEdge> =
        KosarajuStrongConnectivityInspector(graph).condense()

    val biggestCluster = sccs.vertexSet().maxBy { it.size }
    log.info {
      """condensed ${graph.vertexSet().size} classes into ${sccs.vertexSet().size} strongly
connected components. the largest component contains ${biggestCluster.size} classes that all depend
on one another. you will have to break these dependencies manually."""
          .split("\n")
          .joinToString(" ")
    }

    DOTExporter<Set<JavaClass>, DefaultEdge>().apply {
      setVertexAttributeProvider { it.dotAttrs() }
      exportGraph(sccs, System.out)
    }
  }
}

private fun Set<JavaClass>.dotAttrs(): Map<String, Attribute> {
  return listOf(
          "shape" to "box", "label" to map { it.label() }.sorted().joinToString(separator = "\\n"))
      .associate { it.asDotAttr() }
}

private val log = KotlinLogging.logger {}
