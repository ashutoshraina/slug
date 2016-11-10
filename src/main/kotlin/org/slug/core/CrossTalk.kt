package org.slug.core

data class ServiceRegistry(val identifier: String)
class XCtalk(val from: Microservice, val to: Microservice, val using: ServiceRegistry)