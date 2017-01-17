package org.slug.generators

import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.Microservice

object GraphGenerator {
  @Suppress("UNCHECKED_CAST")
  fun <T : MicroserviceGenerator> createServiceGraph(css: String, microservice: Microservice, generatorType: Class<T>): SingleGraph {
    val declaredConstructor = generatorType.constructors.first()
    val generator: T = declaredConstructor.newInstance(microservice) as T

    val name = generator.architecture.identifier
    val graph = SingleGraph(name)

    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

    graph.addAttribute("ui.stylesheet", css)
    generator.addSink(graph)
    generator.begin()
    generator.end()

    graph.addAttribute("ui.antialias")
    graph.addAttribute("ui.quality")

    return graph
  }
}