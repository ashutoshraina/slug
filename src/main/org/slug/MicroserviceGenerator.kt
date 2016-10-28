package org.slug

import org.graphstream.algorithm.generator.Generator
import org.graphstream.stream.SourceBase
import org.slug.Component.DiscoverableComponent
import org.slug.Component.SimpleComponent

class MicroserviceGenerator(val architecture: Microservice) : SourceBase(), Generator {
    override fun end() {
    }

    override fun begin() {
        addLayer()
    }

    override fun nextEvents(): Boolean {
        return true
    }

    private fun addLayer() {
        architecture.layers.forEach(:: println)

        if (architecture.layers.count() == 1) {
            // this isn't a real microservice
            return
        }

        val zippedLayers = layerZipper(architecture.layers)
        zippedLayers.forEach { (first, second) -> createLinkLayers(first.component, first.spatialRedundancy, second.component, second.spatialRedundancy) }
    }

    private fun createLinkLayers(firstLayerComponent: Component, firstLayerRedundancy: Int, secondLayerComponent: Component, secondLayerRedundancy: Int) {

        val froms = addComponent(firstLayerComponent, firstLayerRedundancy)
        val tos = addComponent(secondLayerComponent, secondLayerRedundancy)
        when (firstLayerComponent) {
            is SimpleComponent -> {
                //val outDegree = secondLayerComponent.connection.outDegree
                //val paths = if (outDegree == 1) 1 else Random().nextInt(outDegree - 1) + 1
                for (from in froms) {
                    // pending : probably uses FY Shuffle
                    for (to in tos.take(secondLayerRedundancy)) {
                        Companion.createEdge(this, from, to)
                    }
                }
            }
            is DiscoverableComponent -> {
                froms.zip(tos).forEach { (from, to) -> Companion.createEdge(this, from, to) }
            }
        }
    }

    private fun addComponent(component: Component, redundancy: Int): Sequence<String> {
        var nodes = emptySequence<String>()

        when (component) {
            is SimpleComponent -> {
                for (r in 1..redundancy) {
                    val nodeIdentifier = component.type.identifier + "_" + r
                    nodes = nodes.plus(nodeIdentifier)
                    Companion.createNode(this,nodeIdentifier)
                }
            }
            is DiscoverableComponent -> {
                val nodeIdentifier = component.connection.via.identifier
                //nodes = nodes.plus(nodeIdentifier)
                createNode(this,nodeIdentifier)
                for (r in 1..redundancy) {
                    val nodeIdentifier = component.type.identifier + "_" + r
                    nodes = nodes.plus(nodeIdentifier)
                    Companion.createNode(this,nodeIdentifier)
                    Companion.createEdge(this,component, nodeIdentifier)
                }
                createNode(this,component.connection.to.identifier)
                Companion.createEdge(this, component)
            }
        }
        return nodes
    }

    private companion object {
        fun createNode(microserviceGenerator: MicroserviceGenerator, nodeIdentifier: String) {
            microserviceGenerator.sendNodeAdded(microserviceGenerator.sourceId, nodeIdentifier)
            microserviceGenerator.sendNodeAttributeAdded(microserviceGenerator.sourceId, nodeIdentifier, "ui.label", nodeIdentifier)
        }

        fun createEdge(microserviceGenerator: MicroserviceGenerator, from: String, to: String) {
            val edgeId = "edge_" + from + to
            microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, from, to, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
        }

        fun createEdge(microserviceGenerator: MicroserviceGenerator, component: DiscoverableComponent, nodeIdentifier: String) {
            val edgeId = "edge_" + nodeIdentifier + component.connection.via.identifier
            microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, nodeIdentifier, component.connection.via.identifier, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
        }

        fun createEdge(microserviceGenerator: MicroserviceGenerator, component: DiscoverableComponent) {
            val edgeId = "edge_" + component.connection.via.identifier + component.connection.to.identifier
            microserviceGenerator.sendEdgeAdded(microserviceGenerator.sourceId, edgeId, component.connection.via.identifier, component.connection.to.identifier, true)
//            microserviceGenerator.sendEdgeAttributeAdded(microserviceGenerator.sourceId, edgeId, "ui.label", edgeId)
        }

        fun layerZipper(sequence: Sequence<Layer>): Sequence<Pair<Layer, Layer>> =
                if (sequence.count() == 2) sequenceOf(Pair(sequence.first(), sequence.last())) else sequence.zip(sequence.drop(1))
    }
}