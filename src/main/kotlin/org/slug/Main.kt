package org.slug

import org.graphstream.graph.Graph
import org.slug.core.InfrastructureType
import org.slug.factories.ArchitectureFactory.buildArchitectures
import org.slug.factories.ArchitectureFactory.fromMicroservices
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.factories.buildServiceGraphs
import org.slug.factories.buildServices
import org.slug.generators.LayerGenerator
import org.slug.generators.MicroserviceGenerator
import org.slug.metrics.*
import org.slug.output.DisplayHelper
import org.slug.output.DotConfiguration
import org.slug.output.display
import org.slug.output.generateDotFile
import org.slug.util.Config
import java.io.File
import java.util.concurrent.CompletableFuture

class Main {

    companion object {
        val config = Config.fromConfig("default.properties")

        @JvmStatic fun main(args: Array<String>) {
            val outputFromConfig = config.getProperty("outputDirectory")
            val outputDirectory = if (outputFromConfig.isNullOrEmpty()) "samples" else outputFromConfig

            val file = File(outputDirectory)
            if (!file.exists()) file.mkdirs()

            val styleFile = config.getProperty("style")
            val css = DisplayHelper.loadCSSConfig(styleFile)

            val infrastructure = loadInfrastructureConfig()

            val serviceDensity = config.getProperty("densityFromDistribution")
            val replicationFactor = config.getProperty("replication")
            val powerLaw = config.getBooleanProperty("powerlaw")
            val calculateMetrics = config.getBooleanProperty("metrics")
            val iterations = config.getIntegerProperty("iterations")
            var futures = emptyArray<CompletableFuture<Void>>()
            val crank = Cranks(serviceDensity, replicationFactor, powerLaw)
            var aggregateMetrics = emptySequence<Metric>()

            (1..iterations).forEach { iteration ->

                val dotDirectory = File.separator + "i_" + iteration
                val layerDirectory = dotDirectory + "_l"
                val services = buildServices(MicroserviceFactory(crank, infrastructure))
                val graphs = buildServiceGraphs(services, css, MicroserviceGenerator::class.java)
                val layeredGraphs = buildServiceGraphs(services, css, LayerGenerator::class.java)
                val architectures = fromMicroservices(services, InfrastructureType.ServiceRegistry("Eureka"))
                val crossTalks = buildArchitectures(architectures, css, MicroserviceGenerator::class.java)

                if (calculateMetrics) {
                    futures = futures.plus(CompletableFuture.runAsync {
                        val metrics = measurements(graphs)
                        aggregateMetrics = aggregateMetrics.plus(metrics)
                        display(graphs, DotConfiguration(outputDirectory,dotDirectory))
                    })
                    futures = futures.plus(CompletableFuture.runAsync {
                        val metrics = measurements(layeredGraphs)
                        aggregateMetrics = aggregateMetrics.plus(metrics)
                        display(layeredGraphs, DotConfiguration(outputDirectory,layerDirectory))

                    })
                    futures = futures.plus(CompletableFuture.runAsync {
                        val metrics = measurements(crossTalks)
                        aggregateMetrics = aggregateMetrics.plus(metrics)
                        display(crossTalks, DotConfiguration(outputDirectory,dotDirectory))
                    })
                }
            }

            CompletableFuture.allOf(*futures).get()
            printMetrics(combineMetrics(aggregateMetrics), MetricConfig(outputDirectory, "metrics"))
        }

        private fun display(graphs: Sequence<Graph>, dotConfiguration: DotConfiguration) {
            graphs
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph, dotConfiguration)
                    }
        }

    }
}
