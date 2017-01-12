package org.slug

import org.graphstream.graph.Graph
import org.slug.core.Microservice
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.generators.CrossTalkGenerator.addCrossTalk
import org.slug.generators.GraphGenerator.createServiceGraph
import org.slug.generators.LayerGenerator
import org.slug.generators.MicroserviceGenerator
import org.slug.metrics.MetricConfig
import org.slug.metrics.measurements
import org.slug.metrics.printMetrics
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

            (1..iterations).forEach { iteration ->

                val dotDirectory = File.separator + "i_" + iteration
                val layerDirectory = dotDirectory + "_l"
                val graphs = generateArchitectures(css, MicroserviceFactory(crank, infrastructure), MicroserviceGenerator::class.java, DotConfiguration(outputDirectory, dotDirectory))
                val layeredGraphs = generateArchitectures(css, MicroserviceFactory(crank, infrastructure), LayerGenerator::class.java, DotConfiguration(outputDirectory, layerDirectory))
                if (calculateMetrics) {
                    futures = futures.plus(CompletableFuture.runAsync {
                        printMetrics(measurements(graphs), MetricConfig(outputDirectory, metricsDirectory = dotDirectory))
                    })
                    futures = futures.plus(CompletableFuture.runAsync {
                        printMetrics(measurements(layeredGraphs), MetricConfig(outputDirectory, metricsDirectory = layerDirectory))
                    })
                }
            }
            CompletableFuture.allOf(*futures).get()
        }

        private fun <T : MicroserviceGenerator> generateArchitectures(css: String, microserviceFactory: MicroserviceFactory, generator: Class<T>, dotConfiguration: DotConfiguration): Sequence<Graph> {
            val services = emptySequence<Microservice>()
                    .plusElement(microserviceFactory.simpleArchitecture())
                    .plusElement(microserviceFactory.simple3Tier())
                    .plusElement(microserviceFactory.multipleLinks())
                    .plusElement(microserviceFactory.e2e())
                    .plusElement(microserviceFactory.e2eMultipleApps())
                    .plusElement(microserviceFactory.e2eWithCache())

            val simpleGraphs: Sequence<Graph> = services.map { microservice -> createServiceGraph(css, microservice, generator) }

            val architecture = microserviceFactory.architecture()
            val moreArchitecture = microserviceFactory.someMoreArchitectures()
            val serviceGraphs = architecture.microservices().map { microservice -> createServiceGraph(css, microservice, generator) }
            val moreServiceGraphs = moreArchitecture.microservices().map { microservice -> createServiceGraph(css, microservice, generator) }

            val XTalks = architecture.crossTalks()
            val moreXTalks = moreArchitecture.crossTalks()

            val crossTalks = addCrossTalk(serviceGraphs, XTalks).plus(addCrossTalk(moreServiceGraphs, moreXTalks))

            simpleGraphs.plus(crossTalks)
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph, dotConfiguration)
                    }

            return simpleGraphs.plus(crossTalks)
        }
    }
}
