package org.slug.metrics

import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert
import org.junit.Test
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory
import org.slug.generators.MicroserviceGenerator

class MetricTest {

    val factory = MicroserviceFactory(Cranks("dense", "medium"), Infrastructure.loadInfrastructureConfig("infrastructure.json"))
    @Test
    fun shouldCalculateMetrics() {

        val generator = MicroserviceGenerator(factory.simpleArchitecture())
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        val measurements = measurements(sequenceOf(graph))
        val measurement = measurements.first()
        Assert.assertEquals(SubPlot(3, 4, 1), measurement.subPlot)
        Assert.assertEquals("Graph Id", measurement.chartParams.xLabel)
    }
}