package org.slug

import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.core.Component.DiscoverableComponent
import org.slug.core.Component.SimpleComponent
import org.slug.core.InfrastructureType.*
import org.slug.core.LayerConnection.*


class MicroserviceGeneratorTest {

    val factory = MicroserviceFactory("dense","medium")
    @Test
    fun shouldAddAllTheComponents() {

        val generator = factory.simpleArchitecture()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(18, graph.nodeCount)
        assertEquals(35, graph.edgeCount)

    }

    @Test
    fun shouldCreateLinksBetweenComponents() {
        val generator = factory.simple3Tier()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(19, graph.nodeCount)
        assertEquals(37, graph.edgeCount)
    }

    @Test
    fun canCreateMultipleLayerLinksFromAComponent() {

        val generator = factory.multipleLinks()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(25, graph.nodeCount)
        assertEquals(52, graph.edgeCount)

    }

    @Test
    fun e2eArchitecture() {

        val generator = factory.e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(27, graph.nodeCount)
        assertEquals(54, graph.edgeCount)

    }

    @Test
    fun e2eArchitectureWithMultipleApps() {

        val generator = factory.e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(27, graph.nodeCount)
        assertEquals(54, graph.edgeCount)

    }
}