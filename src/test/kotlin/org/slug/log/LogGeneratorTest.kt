package org.slug.log

import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory
import org.slug.generators.MicroserviceGenerator
import org.slug.util.ResourceHelper

class LogGeneratorTest {

  val densityMap = mapOf("sparse" to 4, "dense" to 10, "hyperdense" to 15)
  val replicationMap = mapOf("minimal" to 3, "medium" to 5, "high" to 7)
  val factory = MicroserviceFactory(Cranks("dense", "medium"), Infrastructure.loadInfrastructureConfig("infrastructure.json"), densityMap, replicationMap)

  @Test
  fun shouldBeAbleToTraceARoute() {

    val generator = MicroserviceGenerator(factory.simple())
    val graph = SingleGraph("First")
    generator.addSink(graph)
    generator.begin()
    generator.end()

    Assert.assertEquals(18, graph.nodeCount)
    Assert.assertEquals(35, graph.edgeCount)

    val templates = ResourceHelper.readTemplates()

    val logGenerator = LogGenerator(templates)
    val seed = graph.getNode<Node>(0)

    logGenerator.tracePath(seed)
  }

  @Test
  fun shouldBeAbleToReadTemplateFiles() {

    val templates = ResourceHelper.readTemplates()

    val template = templates.getTemplate("default")
    assertEquals("default template", template)

    val match = templates.getTemplate("default1234")
    assertEquals("default template", match)
  }
}