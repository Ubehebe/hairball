package jvmutil.deps

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import mu.KotlinLogging

fun readJdepsGraphFromStdin(): ImmutableGraph<JavaPackage> {
  val builder = GraphBuilder.directed().build<JavaPackage>()
  System.`in`.reader().useLines { lines ->
    lines.mapNotNull { parseJdepsLine(it) }.forEach { (from, to) -> builder.putEdge(from, to) }
  }
  return ImmutableGraph.copyOf(builder)
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
