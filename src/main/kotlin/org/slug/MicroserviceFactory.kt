package org.slug

import org.slug.Component.DiscoverableComponent
import org.slug.Component.SimpleComponent
import org.slug.InfrastructureType.*
import org.slug.LayerConnection.*

fun simpleArchitecture(): MicroserviceGenerator {
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

fun simple3Tier(): MicroserviceGenerator {
    val cdn = CDN("Akamai")
    val firewall = Firewall("Juniper")
    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer("1", 1, cdnComponent)

    val proxy = Proxy("NGINX")
    val webApplication = WebApplication("Web_App")
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

fun multipleLinksFromALayer() : MicroserviceGenerator{
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
    val recommendationLayer = Layer("3", 6, recommendationComponent)

    val cassandra = Database("Cassandra")
    val other_dns = ServiceDiscovery("DNS_SERVER_OTHER")
    val web2cassandra = ServiceDiscoveryIndirection(webApplication, other_dns, cassandra)
    val userComponent = DiscoverableComponent(webApplication, web2cassandra)
    val userLayer = Layer("4", 6, userComponent)

    val microservice = Microservice(sequenceOf(cdnLayer, proxyLayer, recommendationLayer, userLayer))
    val generator = MicroserviceGenerator(microservice)
    return generator

}

fun e2e() : MicroserviceGenerator{
    val cdn = CDN("Akamai")
    val firewall = Firewall("Juniper")
    val cdn2Proxy = CDN2Firewall(cdn, firewall, 2)
    val cdnComponent = SimpleComponent(cdn, cdn2Proxy)
    val cdnLayer = Layer("1", 1, cdnComponent)

    val loadBalancer = LoadBalancer("F5")
    val firewall2LoadBalancer = Firewall2LoadBalancer(firewall, loadBalancer, 1)
    val firewallComponent = SimpleComponent(firewall, firewall2LoadBalancer)
    val firewallLayer = Layer("2", 1, firewallComponent)

    val proxy = Proxy("NGINX")
    val loadBalancer2Proxy = LoadBalancer2Proxy(loadBalancer, proxy, 1)
    val loadBalancerComponent = SimpleComponent(loadBalancer, loadBalancer2Proxy)
    val loadBalancerLayer = Layer("3", 1, loadBalancerComponent)

    val webApplication = WebApplication("MyWebApplication")
    val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
    val proxyComponent = SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer("3", 2, proxyComponent)

    val redis = Database("Redis")
    val dns_server = ServiceDiscovery("DNS_SERVER")
    val web2redis = ServiceDiscoveryIndirection(webApplication, dns_server, redis)
    val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
    val recommendationLayer = Layer("4", 5, recommendationComponent)

    val cassandra = Database("Cassandra")
    val other_dns = ServiceDiscovery("DNS_SERVER_OTHER")
    val web2cassandra = ServiceDiscoveryIndirection(webApplication, other_dns, cassandra)
    val userComponent = DiscoverableComponent(webApplication, web2cassandra)
    val userLayer = Layer("5", 5, userComponent)

    val microservice = Microservice(sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, recommendationLayer, userLayer))
    val generator = MicroserviceGenerator(microservice)
    return generator
}