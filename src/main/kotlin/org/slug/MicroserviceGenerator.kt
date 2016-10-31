package org.slug

import org.graphstream.algorithm.generator.Generator
import org.graphstream.stream.SourceBase
import org.slf4j.LoggerFactory
import org.slug.Component.DiscoverableComponent
import org.slug.Component.SimpleComponent

class MicroserviceGenerator(val architecture: Microservice) : SourceBase(), Generator {
    var logger = LoggerFactory.getLogger(javaClass)
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

        for ((first, second) in layerZipper(architecture.layers)) {
            createLinkLayers(first.component, first.spatialRedundancy, second.component, second.spatialRedundancy)
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
                    createNode(this, nodeIdentifier)
                }
            }
            is DiscoverableComponent -> {
                val nodeIdentifier = component.connection.via.identifier
                createNode(this, nodeIdentifier)
                for (r in 1..redundancy) {
                    val nodeIdentifier = createIdentifier(component.type.identifier, r)
                    nodes = nodes.plus(nodeIdentifier)
                    createNode(this, nodeIdentifier)
                    createEdge(this, component, nodeIdentifier)
                }
                createNode(this, component.connection.to.identifier)
                createEdge(this, component)
            }
        }
        createdNodes = createdNodes.plus(nodes)
        return nodes
    }

    private fun createLink(firstLayerComponent: Component, froms: Sequence<String>, secondLayerRedundancy: Int, tos: Sequence<String>) {
        when (firstLayerComponent) {
            is SimpleComponent -> {
                //val outDegree = secondLayerComponent.connection.outDegree
                //val paths = if (outDegree == 1) 1 else Random().nextInt(outDegree - 1) + 1
                for (from in froms) {
                    // pending : probably uses FY Shuffle
                    for (to in tos.take(secondLayerRedundancy)) {
                        createEdge(this, from, to)
                    }
                }
            }
            is DiscoverableComponent -> {
                for ((from, to) in froms.zip(tos)) {
                    createEdge(this, from, to)
                }
            }
        }
    }

    fun createNode(microserviceGenerator: MicroserviceGenerator, nodeIdentifier: String) {
        if (!createdNodes.contains(nodeIdentifier)) {
            logger.debug("creating node " + nodeIdentifier)
            microserviceGenerator.sendNodeAdded(microserviceGenerator.sourceId, nodeIdentifier)
            microserviceGenerator.sendNodeAttributeAdded(microserviceGenerator.sourceId, nodeIdentifier, "ui.label", nodeIdentifier)
        }
    }

    fun createEdge(microserviceGenerator: MicroserviceGenerator, from: String, to: String) {
        val edgeId = from + separator + to
        logger.debug("creating edge " + edgeId)
        microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, from, to, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
    }

    fun createEdge(microserviceGenerator: MicroserviceGenerator, component: DiscoverableComponent, nodeIdentifier: String) {
        val edgeId = nodeIdentifier + separator + component.connection.via.identifier
        logger.debug("creating edge " + edgeId)
        microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, nodeIdentifier, component.connection.via.identifier, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
    }

    fun createEdge(microserviceGenerator: MicroserviceGenerator, component: DiscoverableComponent) {
        val edgeId =  component.connection.via.identifier + separator + component.connection.to.identifier
        logger.debug("creating edge " + edgeId)
        microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, component.connection.via.identifier, component.connection.to.identifier, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
    }

    val separator: String = "->"
    val node_separator : String = "_"

    fun createIdentifier(identifier: String, append: Int) = identifier + node_separator + append

    fun layerZipper(sequence: Sequence<Layer>) =
            if (sequence.count() == 2) sequenceOf(Pair(sequence.first(), sequence.last()))
            else sequence.zip(sequence.drop(1))

    var createdNodes: Sequence<String> = emptySequence()
}