package org.slug

import org.slug.core.*
import org.slug.core.Component.DiscoverableComponent
import org.slug.core.Component.SimpleComponent
import org.slug.core.InfrastructureType.*
import org.slug.core.LayerConnection.*

fun simpleArchitecture(): MicroserviceGenerator {
    val proxy = Proxy("NGINX")
    val webApplication = WebApplication("WebApp")
    val proxy2Web = Proxy2WebApplication(proxy, webApplication, 1)
    val simpleComponent = SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer("1", 2, simpleComponent)

    val database = Database("Redis")
    val serviceDiscovery = ServiceDiscovery("DNS_SERVER")
    val layerConnection = ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
    val discoverableComponent = DiscoverableComponent(webApplication, layerConnection)
    val webLayer = Layer("2", 7, discoverableComponent)

    val microservice = Microservice("simple", sequenceOf(proxyLayer, webLayer))
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

    val microservice = Microservice("simple", sequenceOf(cdnLayer, proxyLayer, webLayer))
    val gen = MicroserviceGenerator(microservice)
    return gen
}

fun multipleLinks(): MicroserviceGenerator {
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

    val microservice = Microservice("multipleLinks", sequenceOf(cdnLayer, proxyLayer, recommendationLayer, userLayer))
    val generator = MicroserviceGenerator(microservice)
    return generator

}

fun e2e(): MicroserviceGenerator {
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
    val proxyLayer = Layer("4", 2, proxyComponent)

    val redis = Database("Redis")
    val dns_server = ServiceDiscovery("DNS_SERVER")
    val web2redis = ServiceDiscoveryIndirection(webApplication, dns_server, redis)
    val recommendationComponent = DiscoverableComponent(webApplication, web2redis)
    val recommendationLayer = Layer("5", 5, recommendationComponent)

    val cassandra = Database("Cassandra")
    val other_dns = ServiceDiscovery("DNS_SERVER_OTHER")
    val web2cassandra = ServiceDiscoveryIndirection(webApplication, other_dns, cassandra)
    val userComponent = DiscoverableComponent(webApplication, web2cassandra)
    val userLayer = Layer("6", 5, userComponent)

    val microservice = Microservice("e2e", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyLayer, recommendationLayer, userLayer))
    val generator = MicroserviceGenerator(microservice)
    return generator
}

fun e2eMultipleApps(): MicroserviceGenerator {
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

    val recommendation = WebApplication("Recommendation")
    val proxy2Recommendation = Proxy2WebApplication(proxy, recommendation, 1)
    val proxyRecommendationComponent = SimpleComponent(proxy, proxy2Recommendation)
    val proxyRecommendationLayer = Layer("4", 2, proxyRecommendationComponent)

    val userCreation = WebApplication("UserCreation")
    val proxy2UserCreation = Proxy2WebApplication(proxy, userCreation, 1)
    val proxyUserCreationComponent = SimpleComponent(proxy, proxy2UserCreation)
    val proxyUserCreationLayer = Layer("5", 2, proxyUserCreationComponent)

    val redis = Database("Redis")
    val dns_server = ServiceDiscovery("DNS_SERVER")
    val web2redis = ServiceDiscoveryIndirection(recommendation, dns_server, redis)
    val recommendationComponent = DiscoverableComponent(recommendation, web2redis)
    val recommendationLayer = Layer("6", 3, recommendationComponent)

    val cassandra = Database("Cassandra")
    val other_dns = ServiceDiscovery("DNS_SERVER_OTHER")
    val web2cassandra = ServiceDiscoveryIndirection(userCreation, other_dns, cassandra)
    val userComponent = DiscoverableComponent(userCreation, web2cassandra)
    val userLayer = Layer("7", 3, userComponent)

    val microservice = Microservice("e2eMultipleApps", sequenceOf(cdnLayer, firewallLayer, loadBalancerLayer, proxyRecommendationLayer, recommendationLayer, proxyUserCreationLayer, userLayer))
    val generator = MicroserviceGenerator(microservice)
    return generator
}

fun multiService(): Architecture {
    val first = e2e().architecture
    val second = e2eMultipleApps().architecture
    val serviceDiscovery = ServiceRegistry("Eureka")
    val crossTalk = Right(XTalk(first, WebApplication("MyWebApplication"), second, WebApplication("Recommendation"), serviceDiscovery))
    val e2e = Left(first)
    val e2eMultipleApps = Left(second)
    return Architecture(sequenceOf(e2e, e2eMultipleApps, crossTalk))
}