package org.slug

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
            val m1 = ChartSeries2DMeasure("Density measure")
            (0..graphs.count() - 1).forEach { r ->
                m1.addValue((r + 1).toDouble(), density(graphs.elementAt(r)))
            }

            val params = ChartMeasure.PlotParameters()
            params.title = "Density Scatter Plot"
            params.type = ChartMeasure.PlotType.LINE
            m1.plot(params)

            val m2 = ChartSeries2DMeasure("Average Degree measure")
            (0..graphs.count() - 1).forEach { r ->
                m2.addValue((r + 1).toDouble(), averageDegree(graphs.elementAt(r)))
            }

            params.title = "Average Degree Scatter Plot"
            params.type = ChartMeasure.PlotType.LINE
            m2.plot(params)

            val m3 = ChartSeries2DMeasure("Average Degree Deviation")
            (0..graphs.count() - 1).forEach { r ->
                m3.addValue((r + 1).toDouble(), degreeAverageDeviation(graphs.elementAt(r)))
            }

            params.title = "Average Degree Deviation"
            params.type = ChartMeasure.PlotType.BAR
            m3.plot(params)
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
