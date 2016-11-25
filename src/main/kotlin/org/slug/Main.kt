package org.slug

import org.graphstream.algorithm.Toolkit
import org.graphstream.algorithm.Toolkit.*
import org.graphstream.algorithm.measure.ChartMeasure
import org.graphstream.algorithm.measure.ChartSeries2DMeasure
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.slug.core.CrossTalkGenerator
import org.slug.core.MicroserviceGenerator
import org.slug.output.DisplayHelper
import org.slug.output.display
import org.slug.output.generateDotFile


class Main {

    companion object {
        val config = Config.fromConfig("default.properties")

        @JvmStatic fun main(args: Array<String>) {
            val styleFile = config.getProperty("style")
            val css = when {
                !styleFile.isNullOrEmpty() -> DisplayHelper().loadCSS(styleFile)
                else -> DisplayHelper().loadDefaultCSS()
            }

            val simpleGraphs: Sequence<Graph> = emptySequence<SingleGraph>().plusElement(generator(css, simpleArchitecture()))
                    .plusElement(generator(css, simple3Tier()))
                    .plusElement(generator(css, multipleLinks()))
                    .plusElement(generator(css, e2e()))
                    .plusElement(generator(css, e2eMultipleApps()))

            val architecture = multiService()
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

        private fun measurements(graphs: Sequence<Graph>) {
            plotMetric(graphs, "Density measure", "Density Scatter Plot", Toolkit::density)
            plotMetric(graphs, "Average Degree measure", "Average Degree Scatter Plot", Toolkit::averageDegree)
            plotMetric(graphs, "Average Degree Deviation", "Average Degree Deviation", Toolkit::degreeAverageDeviation)
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
