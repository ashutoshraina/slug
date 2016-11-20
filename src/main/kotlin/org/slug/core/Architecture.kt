package org.slug.core

import org.slug.MicroserviceGenerator

data class Architecture(private val something: Sequence<Either<Microservice, XTalk>>) {
    fun generators(): Sequence<MicroserviceGenerator> = something.filter { s -> s.isLeft() }.map { l -> MicroserviceGenerator(l.leftValue()) }
    fun crossTalks(): Sequence<XTalk> = something.filter { s -> s.isRight() }.map { l -> l.rightValue() }
}