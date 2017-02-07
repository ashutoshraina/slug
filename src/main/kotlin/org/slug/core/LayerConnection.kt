package org.slug.core

import org.slug.core.InfrastructureType.*
import org.slug.core.Protocol.HTTP
import org.slug.core.Protocol.TCP

sealed class LayerConnection {
  abstract val from: InfrastructureType
  abstract val to: InfrastructureType
  abstract val outDegree: Int
  abstract val protocol: Protocol

  class CDN2Firewall(override val from: CDN,
                     override val to: Firewall,
                     override val outDegree: Int,
                     override val protocol: Protocol = TCP) : LayerConnection()

  class Firewall2LoadBalancer(override val from: Firewall,
                              override val to: LoadBalancer,
                              override val outDegree: Int,
                              override val protocol: Protocol = TCP) : LayerConnection()

  class LoadBalancer2Proxy(override val from: LoadBalancer,
                           override val to: Proxy,
                           override val outDegree: Int,
                           override val protocol: Protocol = HTTP) : LayerConnection()

  class Proxy2WebApplication(override val from: Proxy,
                             override val to: WebApplication,
                             override val outDegree: Int,
                             override val protocol: Protocol = HTTP) : LayerConnection()

  class WebApplication2Cache(override val from: WebApplication,
                             override val to: Cache,
                             override val outDegree: Int,
                             override val protocol: Protocol = TCP) : LayerConnection()

  class Cache2Database(override val from: Cache,
                       override val to: Database,
                       override val outDegree: Int,
                       override val protocol: Protocol = TCP) : LayerConnection()

  class ServiceDiscoveryIndirection(override val from: DiscoverableInfrastructureType,
                                    val via: ServiceDiscovery,
                                    override val to: DiscoverableInfrastructureType,
                                    override val outDegree: Int = 1,
                                    override val protocol: Protocol = TCP) : LayerConnection() {
    override fun toString(): String {
      return "ServiceDiscoveryIndirection(from=$from, via=$via, to=$to, outDegree=$outDegree)"
    }
  }

  override fun toString(): String {
    return "LayerConnection(from=$from, to=$to, outDegree=$outDegree)"
  }
}