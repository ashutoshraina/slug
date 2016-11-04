package org.slug

import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.Component.DiscoverableComponent
import org.slug.Component.SimpleComponent
import org.slug.InfrastructureType.*
import org.slug.LayerConnection.*


class MicroserviceGeneratorTest {

    @Test
    fun shouldAddAllTheComponents() {

        val generator = simpleArchitecture()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(11, graph.nodeCount)
        assertEquals(22, graph.edgeCount)

    }

    @Test
    fun shouldCreateLinksBetweenComponents() {
        val generator = simple3Tier()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(10, graph.nodeCount)
        assertEquals(18, graph.edgeCount)
    }

    @Test
    fun canCreateMultipleLayerLinksFromAComponent() {

        val generator = multipleLinks()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(13, graph.nodeCount)
        assertEquals(28, graph.edgeCount)

    }

    @Test
    fun e2eArchitecture() {

        val generator = e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(14, graph.nodeCount)
        assertEquals(26, graph.edgeCount)

    }

    @Test
    fun e2eArchitectureWithMultipleApps() {

        val generator = e2e()
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(14, graph.nodeCount)
        assertEquals(26, graph.edgeCount)

    }
}