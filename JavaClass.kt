package hairball

data class JavaClass private constructor(val fullyQualified: String) {
  fun label(): String {
    val parts = fullyQualified.split('.')
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  companion object {
    fun parse(fullyQualified: String): JavaClass =
        when {
          fullyQualified.contains("$") -> parse(fullyQualified.split("$").first())
          fullyQualified.endsWith("Kt") ->
              JavaClass(fullyQualified.substring(0, fullyQualified.length - 2))
          else -> JavaClass(fullyQualified)
        }
  }
}
