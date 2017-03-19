package org.slug.factories

import org.slug.core.Microservice
import org.slug.generators.LayerGenerator
import org.slug.generators.MicroserviceGenerator

class ServiceGraphBuilder constructor(){
  constructor(init: ServiceGraphBuilder.() -> Unit) : this() {
    init()
  }
  lateinit var services: Sequence<Microservice>
  lateinit var css: String

  fun setServices(init: ServiceGraphBuilder.() -> Sequence<Microservice>){services = init()}
  fun setCss(init: ServiceGraphBuilder.() -> String) {css = init()}

  fun buildMicros() = buildServiceGraphs(services, css, MicroserviceGenerator::class.java)
  fun buildLayeredMicros() = buildServiceGraphs(services, css, LayerGenerator::class.java)
}