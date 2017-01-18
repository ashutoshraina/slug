package org.slug.factories

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.slug.util.ResourceHelper

class Infrastructure {

  @SerializedName("database")
  @Expose var database: List<String> = emptyList()
  @SerializedName("cache")
  @Expose var cache: List<String> = emptyList()
  @SerializedName("firewall")
  @Expose var firewall: List<String> = emptyList()
  @SerializedName("cdn")
  @Expose var cdn: List<String> = emptyList()
  @SerializedName("proxy")
  @Expose var proxy: List<String> = emptyList()
  @SerializedName("loadBalancer")
  @Expose var loadBalancer: List<String> = emptyList()
  @SerializedName("serviceDiscovery")
  @Expose var serviceDiscovery: List<String> = emptyList()
  @SerializedName("webApplication")
  @Expose var webApplication: List<String> = emptyList()
  @SerializedName("serviceRegistry")
  @Expose var serviceRegistry: List<String> = emptyList()

  fun moveFirstToLast(list: List<String>): List<String> = list.drop(1).plus(list[0])

  fun nextDatabase(): String {
    val result = database[0]
    database = moveFirstToLast(database)
    return result
  }

  fun nextProxy(): String {
    val result = proxy[0]
    proxy = moveFirstToLast(proxy)
    return result
  }

  fun nextCDN(): String {
    val result = cdn[0]
    cdn = moveFirstToLast(cdn)
    return result
  }

  fun nextWebApplication(): String {
    val result = webApplication[0]
    webApplication = moveFirstToLast(webApplication)
    return result
  }

  fun nextFirewall(): String {
    val result = firewall[0]
    firewall = moveFirstToLast(firewall)
    return result
  }

  fun nextCache(): String {
    val result = cache[0]
    cache = moveFirstToLast(cache)
    return result
  }

  fun nextServiceDiscovery(): String {
    val result = serviceDiscovery[0]
    serviceDiscovery = moveFirstToLast(serviceDiscovery)
    return result
  }

  fun nextLoadBalancer(): String {
    val result = loadBalancer[0]
    loadBalancer = moveFirstToLast(loadBalancer)
    return result
  }

  fun nextServiceRegistry(): String {
    val result = serviceRegistry[0]
    serviceRegistry = moveFirstToLast(serviceRegistry)
    return result
  }

  companion object {
    fun loadInfrastructureConfig(file: String = "infrastructure.json"): Infrastructure {
      val content = ResourceHelper.readResourceFile(file)
      return Gson().fromJson<org.slug.factories.Infrastructure>(content)
    }

  }
}

