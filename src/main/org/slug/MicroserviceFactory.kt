package org.slug

import org.graphstream.graph.implementations.SingleGraph

fun customGenerator(css: String) {
    val gen = architectureFactory()
    val graph = SingleGraph("First")

    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

    graph.addAttribute("ui.stylesheet", css)

    gen.addSink(graph)

    gen.begin()
    gen.end()

    graph.addAttribute("ui.antialias")
    graph.addAttribute("ui.quality")

    graph.display()
}

private fun architectureFactory(): MicroserviceGenerator {
    val proxy = InfrastructureType.Proxy("NGINX")
    val webApplication = InfrastructureType.WebApplication("MyWebApplication")
    val proxy2Web = LayerConnection.Proxy2WebApplication(proxy, webApplication, 1)
    val simpleComponent = Component.SimpleComponent(proxy, proxy2Web)
    val proxyLayer = Layer("1", 2, simpleComponent)

    val database = InfrastructureType.Database("Redis")
    val serviceDiscovery = InfrastructureType.ServiceDiscovery("DNS_SERVER")
    val layerConnection = LayerConnection.ServiceDiscoveryIndirection(webApplication, serviceDiscovery, database)
    val discoverableComponent = Component.DiscoverableComponent(webApplication, layerConnection)
    val webLayer = Layer("2", 7, discoverableComponent)

    val microservice = Microservice(sequenceOf(proxyLayer, webLayer))
    val gen = MicroserviceGenerator(microservice)
    return gen
}
