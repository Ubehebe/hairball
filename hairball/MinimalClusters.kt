package hairball

import com.beust.jcommander.Parameters
import hairball.Command
import hairball.dotAttrs
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.dot.DOTExporter

@Parameters(
    commandNames = ["MinimalClusters"],
    commandDescription =
        "break input jar into minimal acyclic clusters (=strongly connected components). writes a .dot graph to stdout.",
    separators = "= ",
)
object MinimalClusters : Command {
  override fun run(stronglyConnectedComponents: Graph<Set<JavaClass>, DefaultEdge>) {
    DOTExporter<Set<JavaClass>, DefaultEdge>().apply {
      setVertexAttributeProvider { it.dotAttrs() }
      exportGraph(stronglyConnectedComponents, System.out)
    }
  }
}

private fun Set<JavaClass>.dotAttrs(): Map<String, Attribute> =
        listOf("shape" to "box", "label" to map { it.label() }.sorted().joinToString(separator = "\\n"))
                .associate { it.asDotAttr() }
