package org.slug

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.CrossTalkGenerator
import org.slug.core.LayerGenerator
import org.slug.core.Microservice
import org.slug.core.MicroserviceGenerator
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.output.DisplayHelper
import org.slug.output.DotConfiguration
import org.slug.output.display
import org.slug.output.generateDotFile
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture


class Main {

    companion object {
        val config = Config.fromConfig("default.properties")

        @JvmStatic fun main(args: Array<String>) {
            val outputFromConfig = config.getProperty("outputDirectory")
            val outputDirectory = if (outputFromConfig.isNullOrEmpty()) "samples" else outputFromConfig

            val file = File(outputDirectory)
            if (!file.exists()) file.mkdirs()

            val css = loadCSSConfig()
            val infrastructure = loadInfrastructureConfig()

            val serviceDensity = config.getProperty("densityFromDistribution")
            val replicationFactor = config.getProperty("replication")
            val powerLawDistribution = config.getBooleanProperty("powerlaw")
            val shouldPlot = config.getBooleanProperty("plots")
            val iterations = config.getIntegerProperty("iterations")
            var futures = emptyArray<CompletableFuture<Void>>()
            val crank = Cranks(serviceDensity, replicationFactor, powerLawDistribution)

            (1..iterations).forEach { iteration ->

                val dotDirectory = File.separator + "i_" + iteration
                val layerDirectory = dotDirectory + "_l"
                val graphs = generateArchitectures(css, MicroserviceFactory(crank, infrastructure), MicroserviceGenerator::class.java, DotConfiguration(outputDirectory, dotDirectory))
                val layeredGraphs = generateArchitectures(css, MicroserviceFactory(crank, infrastructure), LayerGenerator::class.java, DotConfiguration(outputDirectory, layerDirectory))
                futures = futures.plus(CompletableFuture.runAsync {
                    if (shouldPlot) {
                        measurements(graphs, MetricConfig(outputDirectory, metricsDirectory = dotDirectory))
                        measurements(layeredGraphs, MetricConfig(outputDirectory, metricsDirectory = layerDirectory))
                    }
                })
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

            val simpleGraphs: Sequence<Graph> = services.map { microservice -> generator(css, microservice, generator) }

            val architecture = microserviceFactory.architecture()
            val moreArchitecture = microserviceFactory.someMoreArchitectures()
            val serviceGraphs = architecture.microservices().map { microservice -> generator(css, microservice, generator) }
            val moreServiceGraphs = moreArchitecture.microservices().map { microservice -> generator(css, microservice, generator) }

            val XTalks = architecture.crossTalks()
            val moreXTalks = moreArchitecture.crossTalks()

            val crossTalks = CrossTalkGenerator().addCrossTalk(serviceGraphs, XTalks)
                    .plus(CrossTalkGenerator().addCrossTalk(moreServiceGraphs, moreXTalks))

            simpleGraphs.plus(crossTalks)
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph, dotConfiguration)
                    }

            return simpleGraphs.plus(crossTalks)
        }

        private fun loadCSSConfig(): String {
            val styleFile = config.getProperty("style")
            val css = when {
                !styleFile.isNullOrEmpty() -> DisplayHelper().loadCSS(styleFile)
                else -> DisplayHelper().loadDefaultCSS()
            }
            return css
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : MicroserviceGenerator> generator(css: String, microservice: Microservice, generatorType: Class<T>): SingleGraph {
            val declaredConstructor = generatorType.constructors.first()
            var generator: T = declaredConstructor.newInstance(microservice) as T

            val name = generator.architecture.identifier
            val graph = SingleGraph(name)

            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

            graph.addAttribute("ui.stylesheet", css)
            generator.addSink(graph)
            generator.begin()
            generator.end()

            graph.addAttribute("ui.antialias")
            graph.addAttribute("ui.quality")

            return graph
        }

    }
}
