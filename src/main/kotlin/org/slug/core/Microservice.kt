package org.slug.core

data class Microservice(val identifier: String, val layers: Sequence<Layer>)

data class Layer(val layerId: String,
                 val spatialRedundancy: Int,
                 val component: Component)

fun Microservice.validateSize(): Boolean =
        if (this.layers.count() < 2) false else true