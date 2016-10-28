package org.slug

sealed class InfrastructureType {
    abstract val identifier: String

    abstract class DiscoverableInfrastructureType : InfrastructureType()
    data class CDN(override val identifier: String) : InfrastructureType()
    data class LoadBalancer(override val identifier: String) : InfrastructureType()
    data class Proxy(override val identifier: String) : InfrastructureType()
    data class Firewall(override val identifier: String) : InfrastructureType()
    data class Cache(override val identifier: String) : InfrastructureType()
    data class ServiceDiscovery(override val identifier: String) : InfrastructureType()
    data class WebApplication(override val identifier: String) : DiscoverableInfrastructureType()
    data class Database(override val identifier: String) : DiscoverableInfrastructureType()
}

sealed class LayerConnection {
    abstract val from: InfrastructureType
    abstract val to: InfrastructureType
    abstract val outDegree: Int

    data class CDN2Firewall(override val from: InfrastructureType.CDN,
                            override val to: InfrastructureType.Firewall,
                            override val outDegree: Int) : LayerConnection()

    data class Firewall2LoadBalancer(override val from: InfrastructureType.Firewall,
                                     override val to: InfrastructureType.LoadBalancer,
                                     override val outDegree: Int) : LayerConnection()

    data class LoadBalancer2Proxy(override val from: InfrastructureType.LoadBalancer,
                                  override val to: InfrastructureType.Proxy,
                                  override val outDegree: Int) : LayerConnection()

    data class Proxy2WebApplication(override val from: InfrastructureType.Proxy,
                                    override val to: InfrastructureType.WebApplication,
                                    override val outDegree: Int) : LayerConnection()

    data class WebApplication2Cache(override val from: InfrastructureType.WebApplication,
                                    override val to: InfrastructureType.Cache,
                                    override val outDegree: Int) : LayerConnection()

    data class Cache2Database(override val from: InfrastructureType.Cache,
                              override val to: InfrastructureType.Database,
                              override val outDegree: Int) : LayerConnection()

    data class ServiceDiscoveryIndirection(override val from: InfrastructureType.DiscoverableInfrastructureType,
                                           val via: InfrastructureType.ServiceDiscovery,
                                           override val to: InfrastructureType.DiscoverableInfrastructureType,
                                           override val outDegree: Int = 1) : LayerConnection()
}

sealed class Component {
    abstract val type : InfrastructureType
    abstract val connection : LayerConnection
    data class SimpleComponent(override val type: InfrastructureType,
                               override val connection: LayerConnection) : Component()

    data class DiscoverableComponent(override val type: InfrastructureType.DiscoverableInfrastructureType,
                                     override val connection: LayerConnection.ServiceDiscoveryIndirection) : Component()
}

interface Architecture
data class Microservice(val layers: Sequence<Layer>) : Architecture

data class Layer(val layerId: String,
                 val spatialRedundancy: Int,
                 val component: Component) {
    fun getComponentCount(): Int = spatialRedundancy
}
