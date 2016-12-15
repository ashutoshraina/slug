package org.slug.core

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
    class Database(override val identifier: String, val replicationFactor : Int) : DiscoverableInfrastructureType()

    override fun toString(): String {
        return "InfrastructureType(identifier='$identifier')"
    }
}

fun InfrastructureType.Database.withReplication(replicationFactor: Int) : InfrastructureType.Database{
    return InfrastructureType.Database(this.identifier, replicationFactor)
}