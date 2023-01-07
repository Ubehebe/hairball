package jvmutil.deps

data class JavaPackage(val fullyQualified: String) : Comparable<JavaPackage> {

  fun name(): String = fullyQualified

  fun label(): String {
    val parts = fullyQualified.split('.')
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  override fun compareTo(other: JavaPackage): Int = fullyQualified.compareTo(other.fullyQualified)
}
