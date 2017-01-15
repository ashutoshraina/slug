package org.slug.factories

import org.graphstream.graph.Graph
import org.slug.core.Component.DiscoverableComponent
import org.slug.core.Component.SimpleComponent
import org.slug.core.InfrastructureType.*
import org.slug.core.Layer
import org.slug.core.LayerConnection.*
import org.slug.core.Microservice
import org.slug.core.PowerLaw
import org.slug.core.withReplication
import org.slug.factories.InfrastructureFactory.Companion.create
import org.slug.generators.GraphGenerator
import org.slug.generators.MicroserviceGenerator

data class Cranks(val serviceDensity: String, val replicationFactor: String, val powerLaw: Boolean = false)

class MicroserviceFactory(val cranks: Cranks, val infrastructure: Infrastructure) {
    private val defaultDensity = 5
    private val defaultReplication = 3
    val densityMap = mapOf("sparse" to 4, "dense" to 10, "hyperdense" to 15)
    val replicationMap = mapOf("minimal" to 3, "medium" to 5, "high" to 7)
    private val density = densityMap.getOrElse(cranks.serviceDensity) { defaultDensity }
    private val replication = replicationMap.getOrElse(cranks.replicationFactor) { defaultReplication }

    private val cdn = create<CDN>(infrastructure)
    private val firewall = create<Firewall>(infrastructure)
    private val loadBalancer = create<LoadBalancer>(infrastructure)
    private val proxy = create<Proxy>(infrastructure)
    private val webApplication = create<WebApplication>(infrastructure)
    private val cache = create<Cache>(infrastructure)
    private val aDatabase = create<Database>(infrastructure).withReplication(replication)
    private val anotherDatabase = create<Database>(infrastructure).withReplication(replication)
    private val aDNS = create<ServiceDiscovery>(infrastructure)
    private val anotherDNS = create<ServiceDiscovery>(infrastructure)

    private val densityFromDistribution: Int
        get() = if (cranks.powerLaw) {
            PowerLaw().zipf(density)
        } else density

    fun simple(): Microservice {
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val simpleComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("1", 2, simpleComponent)

        val layerConnection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
        val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
        val webLayer = Layer("2", densityFromDistribution, discoverableComponent)

        return Microservice("simple", sequenceOf(proxyLayer, webLayer))
    }

    fun simple3Tier(): Microservice {
        val density = densityFromDistribution
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("2", 2, proxyComponent)

        val layerConnection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
        val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
        val webLayer = Layer("3", density, discoverableComponent)

        return Microservice("simple3Tier", sequenceOf(cdnLayer, proxyLayer, webLayer))
    }

    fun multipleLinks(): Microservice {

        val density = densityFromDistribution
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("2", 2, proxyComponent)

        val indirection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
        val aComponent = DiscoverableComponent(webApplication, indirection)
        val aWebLayer = Layer("3", density, aComponent)

        val anotherIndirection = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
        val anotherComponent = DiscoverableComponent(webApplication, anotherIndirection)
        val anotherWebLayer = Layer("4", density, anotherComponent)

        return Microservice("multipleLinks", sequenceOf(cdnLayer, proxyLayer, aWebLayer, anotherWebLayer))
    }

    fun e2eWithCache(): Microservice {

        val density = densityFromDistribution
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
        val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
        val firewallLayer = Layer("2", 1, firewallComponent)

        val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
        val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
        val loadBalancerLayer = Layer("3", 1, loadBalancerComponent)

        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("4", 2, proxyComponent)

        val layerConnection = WebApplication2Cache(webApplication, cache, replication)
        val component = SimpleComponent(webApplication, layerConnection)
        val layer = Layer("5", density, component)

        val anotherLayerConnection = ServiceDiscoveryIndirection(cache, aDNS, aDatabase, replication)
        val anotherComponent = DiscoverableComponent(cache, anotherLayerConnection)
        val anotherLayer = Layer("6", density, anotherComponent)

        val web2cassandra = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
        val userComponent = DiscoverableComponent(webApplication, web2cassandra)
        val userLayer = Layer("7", density, userComponent)

        return Microservice("e2eWithCache", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, layer, anotherLayer, userLayer))
    }

    fun e2e(): Microservice {

        val density = densityFromDistribution
        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
        val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
        val firewallLayer = Layer("2", 1, firewallComponent)

        val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
        val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
        val loadBalancerLayer = Layer("3", 1, loadBalancerComponent)

        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("4", 2, proxyComponent)

        val indirection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase, replication)
        val component = DiscoverableComponent(webApplication, indirection)
        val layer = Layer("5", density, component)

        val anotherIndirection = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
        val anotherComponent = DiscoverableComponent(webApplication, anotherIndirection)
        val anotherLayer = Layer("6", density, anotherComponent)

        return Microservice("e2e", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, layer, anotherLayer))
    }

    fun e2eMultipleApps(): Microservice {
        val density = densityFromDistribution

        val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
        val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
        val cdnLayer = Layer("1", 1, cdnComponent)

        val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
        val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
        val firewallLayer = Layer("2", 1, firewallComponent)

        val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
        val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
        val loadBalancerLayer = Layer("3", 1, loadBalancerComponent)

        val proxy2Recommendation = Proxy2WebApplication(proxy, webApplication, 1)
        val proxyRecommendationComponent = SimpleComponent(proxy, proxy2Recommendation)
        val proxyRecommendationLayer = Layer("4", 2, proxyRecommendationComponent)

        val userCreation = create<WebApplication>(infrastructure)
        val proxy2UserCreation = Proxy2WebApplication(proxy, userCreation, 1)
        val proxyUserCreationComponent = SimpleComponent(proxy, proxy2UserCreation)
        val proxyUserCreationLayer = Layer("5", 2, proxyUserCreationComponent)

        val web2redis = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
        val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
        val recommendationLayer = Layer("6", density, recommendationComponent)

        val web2cassandra = ServiceDiscoveryIndirection(userCreation, anotherDNS, anotherDatabase)
        val userComponent = DiscoverableComponent(userCreation, web2cassandra)
        val userLayer = Layer("7", density, userComponent)

        return Microservice("e2eMultipleApps", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyRecommendationLayer, recommendationLayer, proxyUserCreationLayer, userLayer))
    }

}

fun <T : MicroserviceGenerator> buildServiceGraphs(services: Sequence<Microservice>, css: String, generator: Class<T>): Sequence<Graph> {

    val simpleGraphs = services.map { microservice -> GraphGenerator.createServiceGraph(css, microservice, generator) }

    return simpleGraphs
}

fun buildServices(microserviceFactory: MicroserviceFactory): Sequence<Microservice> {
    return emptySequence<Microservice>()
            .plusElement(microserviceFactory.simple())
            .plusElement(microserviceFactory.simple3Tier())
            .plusElement(microserviceFactory.multipleLinks())
            .plusElement(microserviceFactory.e2e())
            .plusElement(microserviceFactory.e2eMultipleApps())
            .plusElement(microserviceFactory.e2eWithCache())
}