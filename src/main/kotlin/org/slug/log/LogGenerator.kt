package org.slug.log

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.slug.output.DisplayConstants
import org.slug.output.GraphConstants
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

      val children = next.getEachLeavingEdge<Edge>()

      val grouped = children.groupBy { e -> e.getTargetNode<Node>().id.substringBefore('_') }
      for ((key, value) in grouped){
        val random = randomGenerator.nextInt(value.size)
        val edge: Edge = value[random]
        if(!visited.contains(edge.getTargetNode())){
          toProcess.addLast(edge.getTargetNode())
        }
      }
    }
    return visited
  }

  fun tracePath(seed: Node) {

    traceRoute(seed)
      .map { it.getAttribute<String>(DisplayConstants.LABEL) }
      .map { templates.getTemplate(it) }
      .forEach { template -> LogEventWriter.log(template, { println(it) }) }
  }

  fun tracePath(graph : Graph) {

    val seed = graph.getNode<Node>(0)
    println(seed)
    tracePath(seed)
  }
}

