package hairball

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import java.io.InputStream
import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

fun InputStream.readJdepsClassGraph(): Graph<JavaClass, DefaultEdge> {
  val graph =
      GraphTypeBuilder.directed<JavaClass, DefaultEdge>()
          .edgeClass(DefaultEdge::class.java)
          .buildGraph()
  reader().useLines { lines ->
    lines
        .mapNotNull { it.parseJdepsClassLine() }
        .forEach { (from, to) ->
          graph.addVertex(from)
          graph.addVertex(to)
          graph.addEdge(from, to)
        }
  }
  return AsUnmodifiableGraph(graph)
}

private val splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings()

private fun String.parseJdepsClassLine(): Pair<JavaClass, JavaClass>? {
  val parts = splitter.splitToList(this)
  return when {
    parts.size < 4 || parts[1] != "->" -> {
      log.warn { "malformed line, skipping: $parts" }
      null
    }
    else -> {
      val from = JavaClass.parse(parts[0])
      val to = JavaClass.parse(parts[2])
      if (from != null && to != null && from != to) {
        from to to
      } else {
        null
      }
    }
  }
}

private val log = KotlinLogging.logger {}
