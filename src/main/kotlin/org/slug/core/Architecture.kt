package org.slug.core

import org.slug.core.InfrastructureType.ServiceRegistry
import org.slug.generators.MicroserviceGenerator
import org.slug.util.*

data class Architecture(private val services: Sequence<Either<Microservice, CrossTalk>>) {
  fun generators(): Sequence<MicroserviceGenerator> =
      services.filter { s -> s.isLeft() }.map { l -> MicroserviceGenerator(l.leftValue()) }

  fun microservices(): Sequence<Microservice> =
      services.filter { s -> s.isLeft() }.map { l -> l.leftValue() }

  fun crossTalks(): Sequence<CrossTalk> =
      services.filter { s -> s.isRight() }.map { l -> l.rightValue() }
}

data class Microservice(val identifier: String, val layers: Sequence<Layer>)

data class Layer(val spatialRedundancy: Int, val component: Component)

data class CrossTalk(val from: Microservice, val entryPoint: InfrastructureType, val to: Microservice, val destinationPoint: InfrastructureType, val using: ServiceRegistry)

fun Microservice.validateSize(): Boolean = this.layers.count() >= 2