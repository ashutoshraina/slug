package org.slug.core

sealed class Component {
    abstract val type: InfrastructureType
    abstract val connection: LayerConnection

    class SimpleComponent(override val type: InfrastructureType,
                          override val connection: LayerConnection) : Component() {
        override fun toString(): String {
            return "SimpleComponent(type=$type, connection=$connection)"
        }
    }

    class DiscoverableComponent(override val type: InfrastructureType.DiscoverableInfrastructureType,
                                override val connection: LayerConnection.ServiceDiscoveryIndirection) : Component() {
        override fun toString(): String {
            return "DiscoverableComponent(type=$type, connection=$connection)"
        }
    }
}