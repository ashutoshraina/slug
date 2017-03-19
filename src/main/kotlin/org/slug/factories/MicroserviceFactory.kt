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
import org.slug.generators.LayerGenerator
import org.slug.generators.MicroserviceGenerator

data class Cranks(val serviceDensity: String, val replicationFactor: String, val powerLaw: Boolean = false)

class MicroserviceFactory(val cranks: Cranks, val infrastructure: Infrastructure, densityMap : Map<String, Int>, replicationMap: Map<String,Int>) {
  private val defaultDensity = 5
  private val defaultReplication = 3
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
    val proxyLayer = Layer(2, simpleComponent)

    val layerConnection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
    val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
    val webLayer = Layer(densityFromDistribution, discoverableComponent)

    return Microservice("simple", sequenceOf(proxyLayer, webLayer))
  }

  fun simple3Tier(): Microservice {
    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer(1, cdnComponent)

    val microservice = simple()

    return microservice.copy("simple3Tier", sequenceOf(cdnLayer).plus(microservice.layers))
  }

  fun multipleLinks(): Microservice {

    val microservice = simple3Tier()

    val anotherIndirection = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
    val anotherComponent = DiscoverableComponent(webApplication, anotherIndirection)
    val anotherWebLayer = Layer(density, anotherComponent)

    return microservice.copy(identifier = "multipleLinks", layers = microservice.layers.plus(anotherWebLayer))
  }

  fun e2eWithCache(): Microservice {

    val density = densityFromDistribution
    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer(1, cdnComponent)

    val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
    val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
    val firewallLayer = Layer(1, firewallComponent)

    val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
    val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
    val loadBalancerLayer = Layer(1, loadBalancerComponent)

    val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
    val proxyComponent = SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer(2, proxyComponent)

    val layerConnection = WebApplication2Cache(webApplication, cache, replication)
    val component = SimpleComponent(webApplication, layerConnection)
    val layer = Layer(density, component)

    val anotherLayerConnection = ServiceDiscoveryIndirection(cache, aDNS, aDatabase, replication)
    val anotherComponent = DiscoverableComponent(cache, anotherLayerConnection)
    val anotherLayer = Layer(density, anotherComponent)

    val web2cassandra = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
    val userComponent = DiscoverableComponent(webApplication, web2cassandra)
    val userLayer = Layer(density, userComponent)

    return Microservice("e2eWithCache", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, layer, anotherLayer, userLayer))
  }

  fun e2e(): Microservice {

    val density = densityFromDistribution
    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer(1, cdnComponent)

    val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
    val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
    val firewallLayer = Layer(1, firewallComponent)

    val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
    val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
    val loadBalancerLayer = Layer(1, loadBalancerComponent)

    val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
    val proxyComponent = SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer(2, proxyComponent)

    val indirection = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase, replication)
    val component = DiscoverableComponent(webApplication, indirection)
    val layer = Layer(density, component)

    val anotherIndirection = ServiceDiscoveryIndirection(webApplication, anotherDNS, anotherDatabase)
    val anotherComponent = DiscoverableComponent(webApplication, anotherIndirection)
    val anotherLayer = Layer(density, anotherComponent)

    return Microservice("e2e", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, layer, anotherLayer))
  }

  fun e2eMultipleApps(): Microservice {
    val density = densityFromDistribution

    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer(1, cdnComponent)

    val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
    val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
    val firewallLayer = Layer(1, firewallComponent)

    val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
    val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
    val loadBalancerLayer = Layer(1, loadBalancerComponent)

    val proxy2Recommendation = Proxy2WebApplication(proxy, webApplication, 1)
    val proxyRecommendationComponent = SimpleComponent(proxy, proxy2Recommendation)
    val proxyRecommendationLayer = Layer(2, proxyRecommendationComponent)

    val userCreation = create<WebApplication>(infrastructure)
    val proxy2UserCreation = Proxy2WebApplication(proxy, userCreation, 1)
    val proxyUserCreationComponent = SimpleComponent(proxy, proxy2UserCreation)
    val proxyUserCreationLayer = Layer(2, proxyUserCreationComponent)

    val web2redis = ServiceDiscoveryIndirection(webApplication, aDNS, aDatabase)
    val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
    val recommendationLayer = Layer(density, recommendationComponent)

    val web2cassandra = ServiceDiscoveryIndirection(userCreation, anotherDNS, anotherDatabase)
    val userComponent = DiscoverableComponent(userCreation, web2cassandra)
    val userLayer = Layer(density, userComponent)

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

fun DSLbuildServiceGraphs(init: serviceGraphBuilder.() -> Unit) = serviceGraphBuilder(init).buildMicros()
fun DSLbuildLayeredGraphs(init: serviceGraphBuilder.() -> Unit) = serviceGraphBuilder(init).buildLayeredMicros()
fun DSLbuildServices(init: serviceBuilder.() -> Unit) = serviceBuilder(init).buildFromMicro()


class serviceBuilder constructor(){
  constructor(init: serviceBuilder.() -> Unit) : this() {
    init()
  }
  lateinit var factory: MicroserviceFactory

  fun setFactory(init: serviceBuilder.() -> MicroserviceFactory){factory = init()}

  fun buildFromMicro() = buildServices(factory)
}

class serviceGraphBuilder constructor(){
  constructor(init: serviceGraphBuilder.() -> Unit) : this() {
    init()
  }
  lateinit var services: Sequence<Microservice>
  lateinit var css: String

  fun setServices(init: serviceGraphBuilder.() -> Sequence<Microservice>){services = init()}
  fun setCss(init: serviceGraphBuilder.() -> String) {css = init()}

  fun buildMicros() = buildServiceGraphs(services, css, MicroserviceGenerator::class.java)
  fun buildLayeredMicros() = buildServiceGraphs(services, css, LayerGenerator::class.java)
}
