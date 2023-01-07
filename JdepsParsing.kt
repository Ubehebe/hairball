package jvmutil.deps

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import mu.KotlinLogging

private val splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings()

fun parseJdepsLine(line: String): Pair<Package, Package>? {
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

private val log = KotlinLogging.logger {}
