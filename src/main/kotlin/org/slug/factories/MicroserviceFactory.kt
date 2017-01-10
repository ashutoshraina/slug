package org.slug.factories

import org.slug.core.*
import org.slug.core.Component.DiscoverableComponent
import org.slug.core.Component.SimpleComponent
import org.slug.core.InfrastructureType.*
import org.slug.core.LayerConnection.*
import org.slug.factories.InfrastructureFactory.Companion.create
import org.slug.util.Left
import org.slug.util.Right

data class Cranks(val serviceDensity: String, val replicationFactor: String, val powerLaw: Boolean = false)

class MicroserviceFactory(val cranks : Cranks, val infrastructure: Infrastructure) {
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
    private val serviceDiscovery = ServiceRegistry("Eureka")

    private val densityFromDistribution: Int
        get() = if (cranks.powerLaw) {
            PowerLaw().zipf(density)
        } else density

    fun simpleArchitecture(): Microservice {
        val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
        val simpleComponent = SimpleComponent(proxy, proxy2Web)
        val proxyLayer = Layer("1", 2, simpleComponent)

        val database = Database("Redis", replication)
        val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
        val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
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

        val web2Cache = WebApplication2Cache(webApplication, cache, replication)
        val cacheComponent = SimpleComponent(webApplication, web2Cache)
        val cacheLayer = Layer("5", density, cacheComponent)

        val cache2Redis = ServiceDiscoveryIndirection(cache, aDNS, aDatabase, replication)
        val recommendationComponent = DiscoverableComponent(cache, cache2Redis)
        val recommendationLayer = Layer("6", density, recommendationComponent)

        val web2cassandra = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
        val userComponent = DiscoverableComponent(webApplication, web2cassandra)
        val userLayer = Layer("7", density, userComponent)

        return Microservice("e2eWithCache", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, cacheLayer, recommendationLayer, userLayer))
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

        val web2redis = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase, replication)
        val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
        val recommendationLayer = Layer("5", density, recommendationComponent)

        val web2cassandra = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
        val userComponent = DiscoverableComponent(webApplication, web2cassandra)
        val userLayer = Layer("6", density, userComponent)

        return Microservice("e2e", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, recommendationLayer, userLayer))
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

    fun architecture(): Architecture {
        val from = e2e()
        val to = e2eMultipleApps()
        val anotherFrom = multipleLinks()

        val entry = from.layers.first { l -> l.component.type is InfrastructureType.WebApplication }.component.type
        val exit = to.layers.last { l -> l.component.type is InfrastructureType.WebApplication }.component.type

        val crossTalk = Right(XTalk(from, entry, to, exit, serviceDiscovery))

        val e2e = Left(from)
        val e2eMultipleApps = Left(to)
        val multiLink = Left(anotherFrom)

        val anotherEntry = anotherFrom.layers.first { l -> l.component.type is InfrastructureType.WebApplication }.component.type
        val anotherExit = to.layers.last { l -> l.component.type is InfrastructureType.WebApplication }.component.type

        val moreCrossTalk = Right(XTalk(anotherFrom, anotherEntry, to, anotherExit, serviceDiscovery))

        return Architecture(sequenceOf(e2e, e2eMultipleApps, multiLink, crossTalk, moreCrossTalk))
    }

    fun someMoreArchitectures(): Architecture {
        val from = e2eWithCache()
        val to = e2eMultipleApps()

        val entry = from.layers.first { l -> l.component.type is InfrastructureType.WebApplication }.component.type
        val exit = to.layers.last { l -> l.component.type is InfrastructureType.WebApplication }.component.type

        val crossTalk = Right(XTalk(from, entry, to, exit, serviceDiscovery))

        val e2eWithCache = Left(from)
        val e2eMultipleApps = Left(to)

        return Architecture(sequenceOf(e2eWithCache, e2eMultipleApps, crossTalk))
    }
}
