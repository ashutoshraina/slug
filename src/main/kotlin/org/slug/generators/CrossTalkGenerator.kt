package org.slug.generators

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.implementations.SingleNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slug.core.CrossTalk
import org.slug.output.DisplayConstants.LABEL
import org.slug.output.DisplayConstants.STYLE
import org.slug.output.GraphConstants.NODE_STYLE
import org.slug.output.GraphConstants.SEPARATOR

object CrossTalkGenerator {
  val logger: Logger? = LoggerFactory.getLogger(javaClass)

  fun addCrossTalk(microservices: Sequence<SingleGraph>, crossTalks: Sequence<CrossTalk>): Sequence<SingleGraph> {

    var graphs = emptySequence<SingleGraph>()
    for (crossTalk in crossTalks) {
      val first = microservices.first { m -> m.id.contentEquals(crossTalk.from.identifier) }
      val second = microservices.first { m -> m.id.contentEquals(crossTalk.to.identifier) }
      graphs = graphs.plus(element = mergeWithNodePreservation(first, second, crossTalk))
    }
    return graphs
  }

  private fun mergeWithNodePreservation(from: Graph, to: Graph, crossTalk: CrossTalk): SingleGraph {

    logger?.debug("Generating cross talk for " + from.id + " and " + to.id)
    val first = Graphs.clone(from)
    val second = Graphs.clone(to)

    val mergedGraph = SingleGraph(first.id + SEPARATOR + second.id, true, false)
    Graphs.mergeIn(mergedGraph, first)
    second.getNodeSet<Node>().forEach { n -> mergedGraph.addNode<Node>(mergedIdentifier(n.id)).addAttribute(LABEL, mergedIdentifier(n.id)) }

    second.getEdgeSet<Edge>().forEach { edge ->
      mergedGraph.addEdge<Edge>(mergedIdentifier(edge.id), mergedIdentifier(edge.getSourceNode<Node>().id), mergedIdentifier(edge.getTargetNode<Node>().id), edge.isDirected)
    }

    val gatewayIdentifier = crossTalk.using.identifier
    val gatewayNode = mergedGraph.addNode<SingleNode>(gatewayIdentifier)
    gatewayNode.addAttribute(LABEL, gatewayIdentifier)
    gatewayNode.addAttribute(STYLE, NODE_STYLE)

    val entryNodes = mergedGraph.getEachNode<Node>().filter { n -> n.id.startsWith(crossTalk.entryPoint.identifier) }
    entryNodes.forEach { node -> mergedGraph.addEdge<Edge>(gatewayIdentifier + SEPARATOR + node.id, node.id, gatewayIdentifier, true) }

    val destinationNodes = mergedGraph.getEachNode<Node>().filter { n -> n.id.startsWith(crossTalk.destinationPoint.identifier) }
    destinationNodes.forEach {
      node ->
      val edgeId = gatewayIdentifier + SEPARATOR + node.id
      if (mergedGraph.getEdge<Edge>(edgeId) == null) {
        mergedGraph.addEdge<Edge>(edgeId, gatewayIdentifier, node.id, true)
      }
    }

    return mergedGraph
  }

  private fun mergedIdentifier(original: String): String = original + "_m"
}