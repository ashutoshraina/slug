package org.slug.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.Main
import org.slug.factories.Infrastructure
import org.slug.factories.MicroserviceFactory

class CrossTalkGeneratorTest {

    @Test
    fun addCrossTalk() {
        val infrastructure = Infrastructure.loadInfrastructureConfig()
        val factory = MicroserviceFactory("sparse", "minimal", infrastructure, false)

        val architecture = factory.architecture()
        val serviceGraphs = architecture.generators().map { microservice -> Main.generator("", microservice) }

        val XTalks = architecture.crossTalks()
        val crossTalks = CrossTalkGenerator().addCrossTalk(serviceGraphs, XTalks)

        assertEquals(39, crossTalks.first().nodeCount)
        assertEquals(72, crossTalks.first().edgeCount)

    }

}