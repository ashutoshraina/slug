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
                    .plusElement(generator(css, factory.e2eWithCache()))

            val architecture = factory.architecture()
            val moreArchitecture = factory.someMoreArchitectures()
            val serviceGraphs = architecture.generators().map { microservice -> generator(css, microservice) }
            val moreServiceGraphs = moreArchitecture.generators().map { microservice -> generator(css, microservice) }

            val XTalks = architecture.crossTalks()
            val moreXTalks = moreArchitecture.crossTalks()

            val crossTalks = CrossTalkGenerator().addCrossTalk(serviceGraphs, XTalks)
                    .plus(CrossTalkGenerator().addCrossTalk(moreServiceGraphs, moreXTalks))

            simpleGraphs.plus(crossTalks)
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph, outputDirectory, dotDirectory)
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

        private fun measurements(graphs: Sequence<Graph>, outputDirectory: String, metricDirectory: String) {
            val densityMeasure = Measurement("Density", "Density", Toolkit::density, "Graph Id", "Density")
            val averageDegreeMeasure = Measurement("Average Degree", "Average Degree", Toolkit::averageDegree, "Graph Id", "Average Degree")
            val averageDegreeDeviation = Measurement("Average Degree Deviation", "Average Degree Deviation", Toolkit::degreeAverageDeviation, "Graph Id", "Average Degree Deviation")
            val nodeCount = Measurement("Node Count", "Node Spread", { graph: Graph -> nodeCount(graph) }, "Graph Id", "Nodes")
            val edgeCount = Measurement("Edge Count", "Edge Spread", { graph: Graph -> edgeCount(graph) }, "Graph Id", "Edges")

            val measurements = sequenceOf(densityMeasure, averageDegreeMeasure, averageDegreeDeviation, nodeCount, edgeCount)

            writeMetrics(graphs, measurements, outputDirectory, metricDirectory)

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

        fun nodeCount(graph: Graph): Double = graph.nodeCount.toDouble()
        fun edgeCount(graph: Graph): Double = graph.edgeCount.toDouble()

    }
}
