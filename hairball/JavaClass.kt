package hairball

/**
 * Represents a java class.
 *
 * No analysis is done below the class level (e.g. methods).
 */
data class JavaClass private constructor(val fullyQualified: String) {
  fun label(): String {
    val parts = fullyQualified.split('.')
    return parts.subList(0, parts.size - 1).map { it[0] }.joinToString(separator = ".") +
        ".${parts.last()}"
  }

  companion object {
    /**
     * Parses a fully-qualified class name into a [JavaClass].
     *
     * There are a few situations where it's helpful to return something other than the literal
     * input:
     * - dependencies to/from inner classes should be remapped to their containing class, since
     *   they're part of the same source file.
     * - kotlin generates synthetic classes ending in `Kt` in some situations. Remap FooKt to Foo to
     *   prevent both from appearing in the graph.
     *     - classes named `package-info` are usually low value. Ignore.
     * - classes named `module-info` indicate that the jar being inspected uses java modules
     *   (https://dev.java/learn/modules), and that the developers have already thought about how to
     *   decompose the jar into components. Ignore these. This program is oblivious of java modules,
     *   and it can be productive to use [ProposeClusters] to cluster the classes in a way different
     *   from (possibly better than) the jar's developers intended.
     */
    fun parse(fullyQualified: String): JavaClass? =
        when {
          fullyQualified.contains("$") -> parse(fullyQualified.split("$").first())
          fullyQualified.endsWith("Kt") ->
              JavaClass(fullyQualified.substring(0, fullyQualified.length - 2))
          fullyQualified.endsWith("module-info") || fullyQualified.endsWith("package-info") -> null
          else -> JavaClass(fullyQualified)
        }
  }
}
