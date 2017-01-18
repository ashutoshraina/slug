package org.slug.core

import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory
import org.slug.generators.LayerGenerator

class LayerGeneratorTest {

  @Test
  fun e2eArchitecture() {
    val densityMap = mapOf("sparse" to 4, "dense" to 10, "hyperdense" to 15)
    val replicationMap = mapOf("minimal" to 3, "medium" to 5, "high" to 7)

    val factory = MicroserviceFactory(Cranks("dense", "medium"), Infrastructure.loadInfrastructureConfig("infrastructure.json"), densityMap, replicationMap)

    val generator = LayerGenerator(factory.e2e())
    val graph = SingleGraph("First")
    generator.addSink(graph)
    generator.begin()
    generator.end()

    assertEquals(9, graph.nodeCount)
    assertEquals(8, graph.edgeCount)

  }
}