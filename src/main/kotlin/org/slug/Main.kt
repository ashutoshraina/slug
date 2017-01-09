package org.slug

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.CrossTalkGenerator
import org.slug.core.MicroserviceGenerator
import org.slug.factories.Cranks
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.output.DisplayHelper
import org.slug.output.DotConfiguration
import org.slug.output.display
import org.slug.output.generateDotFile
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
                val graphs = generateArchitectures(css, MicroserviceFactory(crank, infrastructure), DotConfiguration(outputDirectory, dotDirectory))
                futures = futures.plus(CompletableFuture.runAsync {
                    if (shouldPlot) measurements(graphs, MetricConfig(outputDirectory, metricsDirectory = dotDirectory))
                })
            }
            CompletableFuture.allOf(*futures).get()
        }

        private fun generateArchitectures(css: String, microserviceFactory: MicroserviceFactory, dotConfiguration: DotConfiguration): Sequence<Graph> {
            val simpleGraphs: Sequence<Graph> = emptySequence<SingleGraph>()
                    .plusElement(generator(css, microserviceFactory.simpleArchitecture()))
                    .plusElement(generator(css, microserviceFactory.simple3Tier()))
                    .plusElement(generator(css, microserviceFactory.multipleLinks()))
                    .plusElement(generator(css, microserviceFactory.e2e()))
                    .plusElement(generator(css, microserviceFactory.e2eMultipleApps()))
                    .plusElement(generator(css, microserviceFactory.e2eWithCache()))

            val architecture = microserviceFactory.architecture()
            val moreArchitecture = microserviceFactory.someMoreArchitectures()
            val serviceGraphs = architecture.generators().map { microservice -> generator(css, microservice) }
            val moreServiceGraphs = moreArchitecture.generators().map { microservice -> generator(css, microservice) }

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

        fun generator(css: String, generator: MicroserviceGenerator): SingleGraph {
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
