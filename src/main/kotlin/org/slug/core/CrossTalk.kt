package org.slug.core

data class ServiceRegistry(val identifier: String)
class XTalk(val from: Microservice, val entryPoint : InfrastructureType, val to: Microservice, val destinationPoint : InfrastructureType, val using: ServiceRegistry)