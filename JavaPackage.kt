package jvmutil.deps

import com.google.common.base.Splitter

data class JavaPackage(val fullyQualified: String) : Comparable<JavaPackage>, DotNode {

  override fun name(): String = fullyQualified

  override fun label(): String {
    val parts = Splitter.on('.').splitToList(fullyQualified)
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  override fun compareTo(other: JavaPackage): Int = fullyQualified.compareTo(other.fullyQualified)
}
