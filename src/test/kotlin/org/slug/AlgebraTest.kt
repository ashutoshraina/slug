package org.slug

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.core.Component.*
import org.slug.core.InfrastructureType.*
import org.slug.core.LayerConnection.*
import org.slug.core.Layer
import org.slug.core.Microservice

class AlgebraTest {

    @Test
    fun createAnArchitectureWith2Layers() {

        val webApplication = WebApplication("MyWebApplication")
        val database = Database("Redis")
        val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
        val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database, 1)
        val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
        val webLayer = Layer("2", 5, discoverableComponent)

        val proxy = Proxy("NGINX")
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val simpleComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("1", 5, simpleComponent)

        val microservice = Microservice(sequenceOf(proxyLayer, webLayer))

        assertEquals(2, microservice.layers.count())
        assertEquals(5, microservice.layers.first().spatialRedundancy)
    }

    @Test
    fun layersAreZippedCorrectly() {
        val sequence = sequenceOf(1, 2, 3, 4)
        val zip = zipper(sequence)
        val first = zip.first()
        val last = zip.last()
        assertEquals(3, zip.count())
        assertEquals(1, first.first)
        assertEquals(2, first.second)
        assertEquals(3, last.first)
        assertEquals(4, last.second)

        val sequence1 = sequenceOf(1)
        val singleElementZip = zipper(sequence1)
        assertEquals(1, singleElementZip.count())
        assertEquals(1, singleElementZip.first().first)
        assertEquals(1, singleElementZip.first().second)

        val sequence2 = sequenceOf(1, 2)
        val twoElementZip = zipper(sequence2)
        assertEquals(1, twoElementZip.count())
        assertEquals(1, twoElementZip.first().first)
        assertEquals(2, twoElementZip.first().second)

        val sequence3 = sequenceOf(1, 2, 3)
        val threeElementZip = zipper(sequence3)
        assertEquals(2, threeElementZip.count())
        assertEquals(2, threeElementZip.last().first)
    }

    fun <T> zipper(sequence: Sequence<T>): Sequence<Pair<T, T>> {

        if (sequence.count() == 1) {
            return sequenceOf(Pair(sequence.first(), sequence.first()))
        } else if (sequence.count() == 2) {
            return sequenceOf(Pair(sequence.first(), sequence.last()))
        }

        return sequence.zip(sequence.drop(1))

    }
}