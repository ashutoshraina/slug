package org.slug.core

sealed class LayerConnection {
    abstract val from: InfrastructureType
    abstract val to: InfrastructureType
    abstract val outDegree: Int
    abstract val protocol: Protocol

    class CDN2Firewall(override val from: InfrastructureType.CDN,
                       override val to: InfrastructureType.Firewall,
                       override val outDegree: Int,
                       override val protocol: Protocol = Protocol.TCP) : LayerConnection()

    class Firewall2LoadBalancer(override val from: InfrastructureType.Firewall,
                                override val to: InfrastructureType.LoadBalancer,
                                override val outDegree: Int,
                                override val protocol: Protocol = Protocol.TCP) : LayerConnection()

    class LoadBalancer2Proxy(override val from: InfrastructureType.LoadBalancer,
                             override val to: InfrastructureType.Proxy,
                             override val outDegree: Int,
                             override val protocol: Protocol = Protocol.HTTP) : LayerConnection()

    class Proxy2WebApplication(override val from: InfrastructureType.Proxy,
                               override val to: InfrastructureType.WebApplication,
                               override val outDegree: Int,
                               override val protocol: Protocol = Protocol.HTTP) : LayerConnection()

    class WebApplication2Cache(override val from: InfrastructureType.WebApplication,
                               override val to: InfrastructureType.Cache,
                               override val outDegree: Int,
                               override val protocol: Protocol = Protocol.TCP) : LayerConnection()

    class Cache2Database(override val from: InfrastructureType.Cache,
                         override val to: InfrastructureType.Database,
                         override val outDegree: Int,
                         override val protocol: Protocol = Protocol.TCP) : LayerConnection()

    class ServiceDiscoveryIndirection(override val from: InfrastructureType.DiscoverableInfrastructureType,
                                      val via: InfrastructureType.ServiceDiscovery,
                                      override val to: InfrastructureType.DiscoverableInfrastructureType,
                                      override val outDegree: Int = 1,
                                      override val protocol: Protocol = Protocol.TCP) : LayerConnection() {
        override fun toString(): String {
            return "ServiceDiscoveryIndirection(from=$from, via=$via, to=$to, outDegree=$outDegree)"
        }
    }

    override fun toString(): String {
        return "LayerConnection(from=$from, to=$to, outDegree=$outDegree)"
    }
}