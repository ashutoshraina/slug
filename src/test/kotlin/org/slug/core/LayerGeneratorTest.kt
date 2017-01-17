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
    val factory = MicroserviceFactory(Cranks("dense", "medium"), Infrastructure.loadInfrastructureConfig("infrastructure.json"))

    val generator = LayerGenerator(factory.e2e())
    val graph = SingleGraph("First")
    generator.addSink(graph)
    generator.begin()
    generator.end()

    assertEquals(9, graph.nodeCount)
    assertEquals(8, graph.edgeCount)

  }
}