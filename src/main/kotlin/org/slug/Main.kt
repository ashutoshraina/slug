package org.slug

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.CrossTalkGenerator
import org.slug.core.MicroserviceGenerator
import org.slug.factories.Infrastructure
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.output.DisplayHelper
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

            (1..iterations).forEach { iteration ->

                val dotDirectory = File.separator + "i_" + iteration
                val graphs = generateArchitectures(css, infrastructure, powerLawDistribution, replicationFactor, serviceDensity, outputDirectory, dotDirectory)
                futures = futures.plus(CompletableFuture.runAsync {
                    if (shouldPlot) measurements(graphs, outputDirectory, dotDirectory)
                })
            }
            CompletableFuture.allOf(*futures).get()
        }

        private fun generateArchitectures(css: String, infrastructure: Infrastructure, powerLawDistribution: Boolean, replicationFactor: String, serviceDensity: String, outputDirectory: String, dotDirectory: String): Sequence<Graph> {
            val factory = MicroserviceFactory(serviceDensity, replicationFactor, infrastructure, powerLawDistribution)
            val simpleGraphs: Sequence<Graph> = emptySequence<SingleGraph>()
                    .plusElement(generator(css, factory.simpleArchitecture()))
                    .plusElement(generator(css, factory.simple3Tier()))
                    .plusElement(generator(css, factory.multipleLinks()))
                    .plusElement(generator(css, factory.e2e()))
                    .plusElement(generator(css, factory.e2eMultipleApps()))

            val architecture = factory.multiService()
            val serviceGraphs = architecture.generators().map { microservice -> generator(css, microservice) }

            val XTalks = architecture.crossTalks()
            val crossTalks = CrossTalkGenerator().addCrossTalk(serviceGraphs, XTalks)
            simpleGraphs.plus(crossTalks)
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph, outputDirectory, dotDirectory)
                    }

            return simpleGraphs.plus(serviceGraphs)
        }

        private fun loadCSSConfig(): String {
            val styleFile = config.getProperty("style")
            val css = when {
                !styleFile.isNullOrEmpty() -> DisplayHelper().loadCSS(styleFile)
                else -> DisplayHelper().loadDefaultCSS()
            }
            return css
        }

        private fun measurements(graphs: Sequence<Graph>, outputDirectory: String, metricDirectory: String) {
            writeMetrics(graphs, "Density measure", "Density Scatter Plot",
                    Toolkit::density, "Density", outputDirectory, metricDirectory)
            writeMetrics(graphs, "Average Degree measure", "Average Degree Scatter Plot",
                    Toolkit::averageDegree, "Average Degree", outputDirectory, metricDirectory)
            writeMetrics(graphs, "Average Degree Deviation", "Average Degree Deviation",
                    Toolkit::degreeAverageDeviation, "Average Degree Deviation", outputDirectory, metricDirectory)
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
