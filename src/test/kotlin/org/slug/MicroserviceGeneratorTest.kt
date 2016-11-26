package org.slug

import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.core.Component.DiscoverableComponent
import org.slug.core.Component.SimpleComponent
import org.slug.core.InfrastructureType.*
import org.slug.core.LayerConnection.*


class MicroserviceGeneratorTest {

    val factory = MicroserviceFactory("dense")
    @Test
    fun shouldAddAllTheComponents() {

        val generator = factory.simpleArchitecture()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(14, graph.nodeCount)
        assertEquals(31, graph.edgeCount)

    }

    @Test
    fun shouldCreateLinksBetweenComponents() {
        val generator = factory.simple3Tier()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(15, graph.nodeCount)
        assertEquals(33, graph.edgeCount)
    }

    @Test
    fun canCreateMultipleLayerLinksFromAComponent() {

        val generator = factory.multipleLinks()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(17, graph.nodeCount)
        assertEquals(44, graph.edgeCount)

    }

    @Test
    fun e2eArchitecture() {

        val generator = factory.e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(19, graph.nodeCount)
        assertEquals(46, graph.edgeCount)

    }

    @Test
    fun e2eArchitectureWithMultipleApps() {

        val generator = factory.e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(19, graph.nodeCount)
        assertEquals(46, graph.edgeCount)

    }
}