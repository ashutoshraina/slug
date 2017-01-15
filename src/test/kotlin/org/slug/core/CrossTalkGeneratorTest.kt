package org.slug.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.factories.ArchitectureFactory
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory
import org.slug.generators.CrossTalkGenerator.addCrossTalk
import org.slug.generators.GraphGenerator.createServiceGraph
import org.slug.generators.MicroserviceGenerator

class CrossTalkGeneratorTest {

    @Test
    fun addCrossTalk() {
        val infrastructure = Infrastructure.loadInfrastructureConfig()
        val factory = MicroserviceFactory(Cranks("sparse", "minimal"), infrastructure)

        val architecture = ArchitectureFactory.fromMicroservices(sequenceOf(factory.e2e(), factory.e2eWithCache()), InfrastructureType.ServiceRegistry("Eureka")).first()
        val serviceGraphs = architecture.microservices().map { service -> createServiceGraph("", service, MicroserviceGenerator::class.java) }
        val XTalks = architecture.crossTalks()
        val crossTalks = addCrossTalk(serviceGraphs, XTalks)

        assertEquals(1, crossTalks.count())
        assertEquals(39, crossTalks.first().nodeCount)
        assertEquals(76, crossTalks.first().edgeCount)

    }
}
