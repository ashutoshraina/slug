package org.slug.core

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.implementations.SingleNode
import org.slug.output.DisplayConstants.LABEL
import org.slug.output.DisplayConstants.STYLE
import org.slug.output.GraphConstants.NODE_STYLE
import org.slug.output.GraphConstants.SEPARATOR

class CrossTalkGenerator {

    fun addCrossTalk(microservices: Sequence<SingleGraph>, xTalks: Sequence<XTalk>): Sequence<SingleGraph> {

        var graphs = emptySequence<SingleGraph>()
        for (crossTalk in xTalks) {
            val first = microservices.first { m -> m.id.contentEquals(crossTalk.from.identifier) }
            val second = microservices.first { m -> m.id.contentEquals(crossTalk.to.identifier) }
            graphs = graphs.plus(element = mergeWithNodePreservation(first, second, crossTalk))
        }
        return graphs
    }

    private fun mergeWithNodePreservation(from: Graph, to: Graph, crossTalk: XTalk): SingleGraph {
        val mergedGraph = SingleGraph(from.id + SEPARATOR + to.id, true, false)
        Graphs.mergeIn(mergedGraph, from)
        to.getNodeSet<Node>().forEach { n -> mergedGraph.addNode<Node>(mergedIdentifier(n.id)).addAttribute(LABEL, mergedIdentifier(n.id)) }

        for (edge in to.getEdgeSet<Edge>()) {
            mergedGraph.addEdge<Edge>(mergedIdentifier(edge.id), mergedIdentifier(edge.getSourceNode<Node>().id),
                    mergedIdentifier(edge.getTargetNode<Node>().id), edge.isDirected)
        }

        val gatewayIdentifier = crossTalk.using.identifier
        val gatewayNode = mergedGraph.addNode<SingleNode>(gatewayIdentifier)
        gatewayNode.addAttribute(LABEL, gatewayIdentifier)
        gatewayNode.addAttribute(STYLE, NODE_STYLE)

        val entryNodes = mergedGraph.getEachNode<Node>().filter { n -> n.id.startsWith(crossTalk.entryPoint.identifier) }
        entryNodes.forEach { node -> mergedGraph.addEdge<Edge>(gatewayIdentifier + SEPARATOR + node.id, node.id, gatewayIdentifier, true) }

        val destinationNodes = mergedGraph.getEachNode<Node>().filter { n -> n.id.startsWith(crossTalk.destinationPoint.identifier) }
        destinationNodes.forEach { node -> mergedGraph.addEdge<Edge>(gatewayIdentifier + SEPARATOR + node.id, gatewayIdentifier, node.id, true) }

        return mergedGraph
    }

    private fun mergedIdentifier(original: String): String = original + "_m"
}