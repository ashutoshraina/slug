package org.slug.log

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.slug.output.DisplayConstants
import java.util.*

class LogGenerator(val templates: Templates) {
  private val randomGenerator: Random = Random()

  fun traceRoute(seed: Node): HashSet<Node> {

    val visited = LinkedHashSet<Node>()
    val toProcess = LinkedList<Node>()
    toProcess.add(seed)

    while (!toProcess.isEmpty()) {
      val next = toProcess.pop()
      visited.add(next)

      val grouped = next.getEachLeavingEdge<Edge>().groupBy { e -> e.getTargetNode<Node>().id.substringBefore('_') }
      for ((key, value) in grouped) {
        val random = randomGenerator.nextInt(value.size)
        val edge: Edge = value[random]
        if (!visited.contains(edge.getTargetNode())) {
          toProcess.addLast(edge.getTargetNode())
        }
      }
    }
    return visited
  }

  fun tracePath(seed: Node): List<String> {

    return traceRoute(seed)
      .map { it -> Pair(it,it.getAttribute<String>(DisplayConstants.LABEL)) }
      .map { it ->  LogEventWriter.logMessage(templates.getTemplate(it.second), Date().toString(), it.first.id) }
  }

  fun tracePath(graph: Graph): List<String> {

    val seed = graph.getNode<Node>(0)
    return tracePath(seed)
  }
}

