package jvmutil.deps

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

fun readJdepsGraphFromStdin(): Graph<JavaPackage, DefaultEdge> {
  val graph =
      GraphTypeBuilder.directed<JavaPackage, DefaultEdge>()
          .edgeClass(DefaultEdge::class.java)
          .buildGraph()
  System.`in`.reader().useLines { lines ->
    lines
        .mapNotNull { parseJdepsLine(it) }
        .forEach { (from, to) ->
          graph.addVertex(from)
          graph.addVertex(to)
          graph.addEdge(from, to)
        }
  }
  return AsUnmodifiableGraph(graph)
}

private val splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings()

private fun parseJdepsLine(line: String): Pair<JavaPackage, JavaPackage>? {
  val parts = splitter.splitToList(line)
  // fully.qualified.From -> fully.qualified.To jar-name.jar
  return when {
    parts.size < 4 || parts[1] != "->" -> {
      log.warn { "malformed line, skipping: $parts" }
      null
    }
    else -> JavaPackage(parts[0]) to JavaPackage(parts[2])
  }
}

private val log = KotlinLogging.logger {}
