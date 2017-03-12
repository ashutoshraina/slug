package org.slug.factories

import org.graphstream.graph.Graph
import org.slug.core.Architecture
import org.slug.core.InfrastructureType
import org.slug.core.InfrastructureType.ServiceRegistry
import org.slug.core.Microservice
import org.slug.core.XTalk
import org.slug.generators.CrossTalkGenerator
import org.slug.generators.GraphGenerator
import org.slug.generators.MicroserviceGenerator
import org.slug.util.Left
import org.slug.util.Right

object ArchitectureFactory {

  fun fromMicroservices(from: Microservice, to: Microservice, serviceRegistry: ServiceRegistry): Architecture {
    val entry = from.layers.first { l -> l.component.type is InfrastructureType.WebApplication }.component.type
    val exit = to.layers.last { l -> l.component.type is InfrastructureType.WebApplication }.component.type

    val crossTalk = Right(XTalk(from, entry, to, exit, serviceRegistry))

    return Architecture(sequenceOf(Left(from), Left(to), crossTalk))
  }

  fun fromMicroservices(services: Sequence<Microservice>, serviceRegistry: ServiceRegistry): Sequence<Architecture> {

    return services.zip(services.drop(1)).map { zipped -> fromMicroservices(zipped.first, zipped.second, serviceRegistry) }
  }


  fun <T : MicroserviceGenerator> buildArchitectures(architectures: Sequence<Architecture>, css: String, generator: Class<T>): Sequence<Graph> {

    val crossTalks = architectures.map { it -> CrossTalkGenerator.addCrossTalk(it.microservices().map { microservice -> GraphGenerator.createServiceGraph(css, microservice, generator) }, it.crossTalks()) }
        .flatMap { it.asSequence() }

    return crossTalks
  }

  fun DSLBuildArch(init: DSLArchBuilder.() -> Unit) = DSLArchBuilder(init).build()
  fun DSLBuildXTalk(init: DSLXTalkBuilder.() -> Unit) = DSLXTalkBuilder(init).build()

  class DSLArchBuilder private constructor() {
    constructor(init: DSLArchBuilder.() -> Unit) : this() {
      init()
    }
    lateinit var microSeq: Sequence<Microservice>
    lateinit var someInfra: Infrastructure

    fun setSeq(init: DSLArchBuilder.() -> Sequence<Microservice>) {microSeq = init()}
    fun setInfrastructure(init: DSLArchBuilder.() -> Infrastructure) {someInfra = init()}
    fun build() = fromMicroservices(microSeq, InfrastructureFactory.create<ServiceRegistry>(someInfra))
  }

  class DSLXTalkBuilder private constructor(){
    constructor(init: DSLXTalkBuilder.() -> Unit) : this() {
      init()
    }
    lateinit var archSeq: Sequence<Architecture>
    lateinit var css: String

    fun setSeq(init: DSLXTalkBuilder.() -> Sequence<Architecture>) {archSeq = init()}
    fun setCss(init: DSLXTalkBuilder.() -> String) {css = init()}
    fun build() = buildArchitectures(archSeq, css, MicroserviceGenerator::class.java)
  }
}