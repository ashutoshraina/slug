package org.slug

interface Architecture
data class Microservice(val layers: Sequence<Layer>) : Architecture

data class Layer(val layerId: String,
                 val spatialRedundancy: Int,
                 val component: Component)
