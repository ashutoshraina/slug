package org.slug

import org.graphstream.algorithm.generator.Generator
import org.graphstream.stream.SourceBase
import org.slf4j.LoggerFactory
import org.slug.Component.DiscoverableComponent
import org.slug.Component.SimpleComponent

class MicroserviceGenerator(val architecture: Microservice) : SourceBase(), Generator {
    var logger = LoggerFactory.getLogger(javaClass)
    val separator: String = "->"
    val node_separator: String = "_"
    var createdNodes: Sequence<String> = emptySequence()

    override fun end() {
    }

    override fun begin() {
        addLayer()
    }

    override fun nextEvents(): Boolean {
        return true
    }

    private fun addLayer() {

        if (architecture.layers.count() == 1) {
            // this isn't a real architecture, it has just one layer.
            return
        }

        for ((first, second) in
        layerZipper(architecture.layers)) {
            if (first.component.type.identifier == second.component.type.identifier) {
                addComponent(second.component, second.spatialRedundancy)
            } else {
                createLinkLayers(first.component, first.spatialRedundancy, second.component, second.spatialRedundancy)
            }
        }
    }

    private fun createLinkLayers(firstLayerComponent: Component, firstLayerRedundancy: Int, secondLayerComponent: Component, secondLayerRedundancy: Int) {

        val froms = addComponent(firstLayerComponent, firstLayerRedundancy)
        val tos = addComponent(secondLayerComponent, secondLayerRedundancy)
        createLink(firstLayerComponent, froms, secondLayerRedundancy, tos)
    }

    private fun addComponent(component: Component, redundancy: Int): Sequence<String> {
        var nodes = emptySequence<String>()

        when (component) {
            is SimpleComponent -> {
                for (r in 1..redundancy) {
                    val nodeIdentifier = createIdentifier(component.type.identifier, r)
                    nodes = nodes.plus(nodeIdentifier)
                    createNode(sourceId, nodeIdentifier)
                }
            }
            is DiscoverableComponent -> {
                val nodeIdentifier = component.connection.via.identifier
                createNode(sourceId, nodeIdentifier)
                (1..redundancy).forEach { r ->
                    val from = createIdentifier(component.type.identifier, r)
                    nodes = nodes.plus(from)
                    createNode(sourceId, from)
                    createEdge(sourceId, component, from)
                }
                createNode(sourceId, component.connection.to.identifier)
                createEdge(sourceId, component)
            }
        }
        createdNodes = createdNodes.plus(nodes)
        return nodes
    }

    private fun createLink(firstLayerComponent: Component, froms: Sequence<String>, secondLayerRedundancy: Int, tos: Sequence<String>) {
        when (firstLayerComponent) {
            is SimpleComponent -> {
                for (from in froms) {
                    for (to in tos.take(secondLayerRedundancy)) {
                        createEdge(sourceId, from, to)
                    }
                }
            }
            is DiscoverableComponent -> {
                for ((from, to) in froms.zip(tos)) {
                    createEdge(sourceId, from, to)
                }
            }
        }
    }

    fun createNode(sourceId: String, nodeIdentifier: String) {
        if (!createdNodes.contains(nodeIdentifier)) {
            logger.debug("creating node " + nodeIdentifier)
            sendNodeAdded(sourceId, nodeIdentifier)
            sendNodeAttributeAdded(sourceId, nodeIdentifier, "ui.label", nodeIdentifier)
        }
    }

    fun createEdge(sourceId: String, from: String, to: String) {
        val edgeId = from + separator + to
        logger.debug("creating edge " + edgeId)
        sendEdgeAdded(sourceId, edgeId, from, to, true)
    }

    fun createEdge(sourceId: String, component: DiscoverableComponent, nodeIdentifier: String) {
        val edgeId = nodeIdentifier + separator + component.connection.via.identifier
        logger.debug("creating edge " + edgeId)
        sendEdgeAdded(sourceId, edgeId, nodeIdentifier, component.connection.via.identifier, true)
    }

    fun createEdge(sourceId : String, component: DiscoverableComponent) {
        val edgeId = component.connection.via.identifier + separator + component.connection.to.identifier
        logger.debug("creating edge " + edgeId)
        sendEdgeAdded(sourceId, edgeId, component.connection.via.identifier, component.connection.to.identifier, true)
    }

    fun createIdentifier(identifier: String, append: Int) = identifier + node_separator + append

    fun layerZipper(sequence: Sequence<Layer>) =
            if (sequence.count() == 2) sequenceOf(Pair(sequence.first(), sequence.last()))
            else sequence.zip(sequence.drop(1))

}