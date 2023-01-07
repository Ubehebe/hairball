package jvmutil.deps

data class JavaClass(val fullyQualified: String) {
  fun isInnerClass(): Boolean = "$" in fullyQualified

  fun isSyntheticKotlinClass(): Boolean = fullyQualified.endsWith("Kt")

  fun label(): String {
    val parts = fullyQualified.split('.')
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }
}
