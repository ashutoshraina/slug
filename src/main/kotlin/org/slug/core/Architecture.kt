package org.slug.core

import org.slug.util.*

data class Architecture(private val services: Sequence<Either<Microservice, XTalk>>) {
    fun generators(): Sequence<MicroserviceGenerator> = services.filter { s -> s.isLeft() }.map { l -> MicroserviceGenerator(l.leftValue()) }
    fun microservices(): Sequence<Microservice> = services.filter { s -> s.isLeft() }.map { l -> l.leftValue() }
    fun crossTalks(): Sequence<XTalk> = services.filter { s -> s.isRight() }.map { l -> l.rightValue() }
}