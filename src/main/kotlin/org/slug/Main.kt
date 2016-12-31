package org.slug

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.CrossTalkGenerator
import org.slug.core.MicroserviceGenerator
import org.slug.factories.Infrastructure.Companion.loadInfrastructureConfig
import org.slug.factories.MicroserviceFactory
import org.slug.output.DisplayHelper
import org.slug.output.display
import org.slug.output.generateDotFile
import java.io.File

class Main {

    companion object {
        val config = Config.fromConfig("default.properties")

        @JvmStatic fun main(args: Array<String>) {
            val css = loadCSSConfig()
            val infrastructure = loadInfrastructureConfig()

            val factory = MicroserviceFactory(config.getProperty("densityFromDistribution"), config.getProperty("replication"), infrastructure ,config.getBooleanProperty("powerlaw"))
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
            serviceGraphs.plus(crossTalks)
                    .forEach { graph ->
                        if (config.getBooleanProperty("display.swing")) display(graph)
                        if (config.getBooleanProperty("display.dot")) generateDotFile(graph)
                    }

            val graphs = simpleGraphs.plus(serviceGraphs)
            measurements(graphs)
        }


        private fun loadCSSConfig(): String {
            val file = File("samples")
            if (!file.exists()) file.mkdirs()
            val styleFile = config.getProperty("style")
            val css = when {
                !styleFile.isNullOrEmpty() -> DisplayHelper().loadCSS(styleFile)
                else -> DisplayHelper().loadDefaultCSS()
            }
            return css
        }

        private fun measurements(graphs: Sequence<Graph>) {
            plotMetric(graphs, "Density measure", "Density Scatter Plot", Toolkit::density, "Density")
            plotMetric(graphs, "Average Degree measure", "Average Degree Scatter Plot", Toolkit::averageDegree, "Average Degree")
            plotMetric(graphs, "Average Degree Deviation", "Average Degree Deviation", Toolkit::degreeAverageDeviation, "Average Degree Deviation")
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
