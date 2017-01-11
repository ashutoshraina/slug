package org.slug.generators

import org.slug.core.Component
import org.slug.core.InfrastructureType
import org.slug.core.Microservice
import org.slug.core.validateSize

class LayerGenerator(architecture: Microservice) : MicroserviceGenerator(architecture) {

    override fun begin() {
        addLayer()
    }

    private fun addLayer() {
        if (!architecture.validateSize()) {
            // this isn't a real architecture, it has just one layer.
            return
        }

        for ((first, second) in
        layerZipper(architecture.layers)) {
            when {
                areEqual(first, second) -> addComponent(second.component)
                else -> createLinkLayers(first.component, second.component)
            }
        }
    }

    private fun createLinkLayers(firstLayerComponent: Component, secondLayerComponent: Component) {
        val froms = addComponent(firstLayerComponent)
        val tos = addComponent(secondLayerComponent)
        createLink(firstLayerComponent, froms, tos)
    }

    private fun addComponent(component: Component): Sequence<String> {
        logger?.debug("adding component " + component)
        var nodes = emptySequence<String>()

        when (component) {
            is Component.SimpleComponent -> {
                nodes = nodes.plus(createIdentifier(component.type.identifier))
                createNode(createIdentifier(component.type.identifier))
            }
            is Component.DiscoverableComponent -> {
                createNode(component.connection.via.identifier)
                val from = createIdentifier(component.type.identifier)
                nodes = nodes.plus(from)
                createNode(from)
                createEdge(component, from)
                when (component.connection.to) {
                    is InfrastructureType.Database -> {
                        createNode(createIdentifier(component.connection.to.identifier))
                        createEdge(component.connection.via.identifier, createIdentifier(component.connection.to.identifier))
                    }
                    else -> {
                        createNode(component.connection.to.identifier)
                        createEdge(component)
                    }
                }
            }
        }
        return nodes
    }

    private fun createLink(firstLayerComponent: Component, froms: Sequence<String>, tos: Sequence<String>) =
            when (firstLayerComponent) {
                is Component.SimpleComponent -> {
                    logger?.debug("creating link for simple component " + firstLayerComponent)
                    froms.forEach { from ->
                        tos.forEach { to -> createEdge(from, to) }
                    }
                }
                is Component.DiscoverableComponent -> {
                    logger?.debug("creating link for discoverable component " + firstLayerComponent)
                    for ((from, to) in froms.zip(tos)) {
                        createEdge(from, to)
                    }
                }
            }

    fun createIdentifier(identifier: String) = identifier

}
