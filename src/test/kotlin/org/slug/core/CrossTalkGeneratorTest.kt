package org.slug.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.Main
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory
import org.slug.generators.CrossTalkGenerator.addCrossTalk
import org.slug.generators.MicroserviceGenerator

class CrossTalkGeneratorTest {

    @Test
    fun addCrossTalk() {
        val infrastructure = Infrastructure.loadInfrastructureConfig()
        val factory = MicroserviceFactory(Cranks("sparse", "minimal"), infrastructure)

        val architecture = factory.architecture()
        val serviceGraphs = architecture.generators().map { service -> Main.generator("", service.architecture, MicroserviceGenerator::class.java) }

        val XTalks = architecture.crossTalks()
        val crossTalks = addCrossTalk(serviceGraphs, XTalks)

        assertEquals(2, crossTalks.count())
        assertEquals(39, crossTalks.first().nodeCount)
        assertEquals(72, crossTalks.first().edgeCount)

    }

}
