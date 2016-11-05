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