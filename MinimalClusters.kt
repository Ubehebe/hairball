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

    log.info {
      "condensed ${graph.vertexSet().size} vertices into ${sccs.vertexSet().size} strongly connected components"
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
