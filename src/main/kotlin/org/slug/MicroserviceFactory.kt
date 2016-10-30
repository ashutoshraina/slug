package org.slug

import org.slug.InfrastructureType.*
import org.slug.LayerConnection.*
import org.slug.Component.*

fun architectureFactory(): MicroserviceGenerator {
    val proxy = Proxy("NGINX")
    val webApplication = WebApplication("MyWebApplication")
    val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
    val simpleComponent = SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer("1", 2, simpleComponent)

    val database = Database("Redis")
    val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
    val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
    val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
    val webLayer = Layer("2", 7, discoverableComponent)

    val microservice = Microservice(sequenceOf(proxyLayer, webLayer))
    val gen = MicroserviceGenerator(microservice)
    return gen
}

fun architectureFactoryWith3Layers(): MicroserviceGenerator {
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

    val database = Database("Redis")
    val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
    val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
    val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
    val webLayer = Layer("3", 5, discoverableComponent)

    val microservice = Microservice(sequenceOf(cdnLayer, proxyLayer, webLayer))
    val gen = MicroserviceGenerator(microservice)
    return gen
}