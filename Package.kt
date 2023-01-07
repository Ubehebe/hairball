package jvmutil.deps

import com.google.common.base.Splitter

data class Package(val fullyQualified: String) : Comparable<Package>, DotNode {

  override fun toString(): String = "\"$fullyQualified\""

  override fun label(): String {
    val parts = Splitter.on('.').splitToList(fullyQualified)
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  override fun compareTo(other: Package): Int = fullyQualified.compareTo(other.fullyQualified)
}
