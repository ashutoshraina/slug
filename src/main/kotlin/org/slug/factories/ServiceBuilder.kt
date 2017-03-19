package org.slug.factories

class ServiceBuilder constructor(){
  constructor(init: ServiceBuilder.() -> Unit) : this() {
    init()
  }
  lateinit var factory: MicroserviceFactory

  fun setFactory(init: ServiceBuilder.() -> MicroserviceFactory){factory = init()}

  fun buildFromMicro() = buildServices(factory)
}
fun buildServiceGraphs(init: ServiceGraphBuilder.() -> Unit) = ServiceGraphBuilder(init).buildMicros()
fun buildLayeredGraphs(init: ServiceGraphBuilder.() -> Unit) = ServiceGraphBuilder(init).buildLayeredMicros()
fun buildServices(init: ServiceBuilder.() -> Unit) = ServiceBuilder(init).buildFromMicro()