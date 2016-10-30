package org.slug

sealed class InfrastructureType {
    abstract val identifier: String


    abstract class DiscoverableInfrastructureType : InfrastructureType()

    class CDN(override val identifier: String) : InfrastructureType()
    class LoadBalancer(override val identifier: String) : InfrastructureType()
    class Proxy(override val identifier: String) : InfrastructureType()
    class Firewall(override val identifier: String) : InfrastructureType()
    class Cache(override val identifier: String) : InfrastructureType()
    class ServiceDiscovery(override val identifier: String) : InfrastructureType()
    class WebApplication(override val identifier: String) : DiscoverableInfrastructureType()
    class Database(override val identifier: String) : DiscoverableInfrastructureType()

    override fun toString(): String {
        return "InfrastructureType(identifier='$identifier')"
    }
}

sealed class LayerConnection {
    abstract val from: InfrastructureType
    abstract val to: InfrastructureType
    abstract val outDegree: Int

    class CDN2Firewall(override val from: InfrastructureType.CDN,
                       override val to: InfrastructureType.Firewall,
                       override val outDegree: Int) : LayerConnection()

    class Firewall2LoadBalancer(override val from: InfrastructureType.Firewall,
                                override val to: InfrastructureType.LoadBalancer,
                                override val outDegree: Int) : LayerConnection()

    class LoadBalancer2Proxy(override val from: InfrastructureType.LoadBalancer,
                             override val to: InfrastructureType.Proxy,
                             override val outDegree: Int) : LayerConnection()

    class Proxy2WebApplication(override val from: InfrastructureType.Proxy,
                               override val to: InfrastructureType.WebApplication,
                               override val outDegree: Int) : LayerConnection()

    class WebApplication2Cache(override val from: InfrastructureType.WebApplication,
                               override val to: InfrastructureType.Cache,
                               override val outDegree: Int) : LayerConnection()

    class Cache2Database(override val from: InfrastructureType.Cache,
                         override val to: InfrastructureType.Database,
                         override val outDegree: Int) : LayerConnection()

    class ServiceDiscoveryIndirection(override val from: InfrastructureType.DiscoverableInfrastructureType,
                                      val via: InfrastructureType.ServiceDiscovery,
                                      override val to: InfrastructureType.DiscoverableInfrastructureType,
                                      override val outDegree: Int = 1) : LayerConnection() {
        override fun toString(): String {
            return "ServiceDiscoveryIndirection(from=$from, via=$via, to=$to, outDegree=$outDegree)"
        }
    }

    override fun toString(): String {
        return "LayerConnection(from=$from, to=$to, outDegree=$outDegree)"
    }
}

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

interface Architecture
class Microservice(val layers: Sequence<Layer>) : Architecture

data class Layer(val layerId: String,
                 val spatialRedundancy: Int,
                 val component: Component) {
    fun getComponentCount(): Int = spatialRedundancy
}
