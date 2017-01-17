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
}