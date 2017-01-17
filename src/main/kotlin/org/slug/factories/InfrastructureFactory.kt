package org.slug.factories

import org.slug.core.InfrastructureType
import org.slug.core.InfrastructureType.*

abstract class InfrastructureFactory<out T : InfrastructureType> {
  abstract fun create(): T

  companion object {
    inline fun <reified T> create(infrastructure: Infrastructure): T = when (T::class) {
      Database::class -> DatabaseFactory(infrastructure).create() as T
      Cache::class -> CacheFactory(infrastructure).create() as T
      Proxy::class -> ProxyFactory(infrastructure).create() as T
      LoadBalancer::class -> LoadBalancerFactory(infrastructure).create() as T
      WebApplication::class -> WebApplicationFactory(infrastructure).create() as T
      CDN::class -> CDNFactory(infrastructure).create() as T
      Firewall::class -> FirewallFactory(infrastructure).create() as T
      ServiceDiscovery::class -> ServiceDiscoveryFactory(infrastructure).create() as T
      ServiceRegistry::class -> ServiceRegistryFactory(infrastructure).create() as T
      else -> throw IllegalArgumentException()
    }
  }

}

class DatabaseFactory(val infrastructure: Infrastructure) : InfrastructureFactory<Database>() {
  override fun create(): Database = Database(infrastructure.nextDatabase(), 5)
}

class CacheFactory(val infrastructure: Infrastructure) : InfrastructureFactory<Cache>() {
  override fun create(): Cache = Cache(infrastructure.nextCache())
}

class ProxyFactory(val infrastructure: Infrastructure) : InfrastructureFactory<Proxy>() {
  override fun create() = Proxy(infrastructure.nextProxy())
}

class LoadBalancerFactory(val infrastructure: Infrastructure) : InfrastructureFactory<LoadBalancer>() {
  override fun create() = LoadBalancer(infrastructure.nextLoadBalancer())
}

class WebApplicationFactory(val infrastructure: Infrastructure) : InfrastructureFactory<WebApplication>() {
  override fun create() = WebApplication(infrastructure.nextWebApplication())
}

class CDNFactory(val infrastructure: Infrastructure) : InfrastructureFactory<CDN>() {
  override fun create() = CDN(infrastructure.nextCDN())
}

class FirewallFactory(val infrastructure: Infrastructure) : InfrastructureFactory<Firewall>() {
  override fun create() = Firewall(infrastructure.nextFirewall())
}

class ServiceDiscoveryFactory(val infrastructure: Infrastructure) : InfrastructureFactory<ServiceDiscovery>() {
  override fun create() = ServiceDiscovery(infrastructure.nextServiceDiscovery())
}

class ServiceRegistryFactory(val infrastructure: Infrastructure) : InfrastructureFactory<ServiceRegistry>() {
  override fun create() = ServiceRegistry(infrastructure.nextServiceRegistry())
}
