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
    fun generatorShouldAddAllTheComponentsInTheArchitecture() {
        val proxy = Proxy("NGINX")
        val webApplication = WebApplication("MyWebApplication")
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val simpleComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("1", 2, simpleComponent)

        val database = Database("Redis")
        val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
        val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
        val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
        val webLayer = Layer("2", 5, discoverableComponent)

        val microservice = Microservice(sequenceOf(proxyLayer, webLayer))
        val generator = MicroserviceGenerator(microservice)
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(9, graph.nodeCount)
        assertEquals(16, graph.edgeCount)

    }

    @Test
    fun generatorShouldCreateLinksBetweenTheSpecifiedLayers() {
        val cdn = CDN("Akamai")
        val firewall = Firewall("Juniper")
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val proxy = Proxy("NGINX")
        val webApplication = WebApplication("MyWebApplication")
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("2", 2, proxyComponent)

        val redis = Database("Redis")
        val dns_server = ServiceDiscovery("DNS_SERVER")
        val web2redis = ServiceDiscoveryIndirection(webApplication, dns_server, redis)
        val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
        val recommendationLayer = Layer("3", 5, recommendationComponent)

        val microservice = Microservice(sequenceOf(cdnLayer, proxyLayer, recommendationLayer))
        val generator = MicroserviceGenerator(microservice)
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(10, graph.nodeCount)
        assertEquals(18, graph.edgeCount)
    }

    @Test
    fun multipleLayerLinksFromAComponent() {
        val cdn = CDN("Akamai")
        val firewall = Firewall("Juniper")
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val proxy = Proxy("NGINX")
        val webApplication = WebApplication("MyWebApplication")
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("2", 2, proxyComponent)

        val redis = Database("Redis")
        val dns_server = ServiceDiscovery("DNS_SERVER")
        val web2redis = ServiceDiscoveryIndirection(webApplication, dns_server, redis)
        val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
        val recommendationLayer = Layer("3", 5, recommendationComponent)

        val cassandra = Database("Cassandra")
        val other_dns = ServiceDiscovery("DNS_SERVER_OTHER")
        val web2cassandra = ServiceDiscoveryIndirection(webApplication, other_dns, cassandra)
        val userComponent = DiscoverableComponent(webApplication, web2cassandra)
        val userLayer = Layer("4", 5, userComponent)

        val microservice = Microservice(sequenceOf(cdnLayer, proxyLayer, recommendationLayer, userLayer))
        val generator = MicroserviceGenerator(microservice)
        val graph = SingleGraph("First")
        generator.addSink(graph)
        generator.begin()
        generator.end()

        assertEquals(12, graph.nodeCount)
        assertEquals(24, graph.edgeCount)

    }
}