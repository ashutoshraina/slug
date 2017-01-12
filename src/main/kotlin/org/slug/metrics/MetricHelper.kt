package org.slug.metrics

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph
import java.nio.file.Files

data class MetricConfig(val outputDirectory: String, val metricsDirectory: String)
data class SubPlot(val rows: Int, val cols: Int, val position: Int)
data class ChartParams(val xLabel: String, val yLabel: String, val legend: String, val title: String)
class ChartData(val xValues: Array<Int>, val yValues: Array<Double>)

data class Metric(val chartData: ChartData, val chartParams: ChartParams, val subPlot: SubPlot) {
    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.appendln("@subplot(${subPlot.rows},${subPlot.cols},${subPlot.position})")
        buffer.appendln(chartData.xValues.joinToString(",", "x = [", "];"))
        buffer.appendln(chartData.yValues.joinToString(",", "y = [", "];"))
        buffer.appendln("plot (x,y);")
        buffer.appendln("xlabel (\"${chartParams.xLabel}\");")
        buffer.appendln("ylabel (\"${chartParams.yLabel}\");")
        buffer.appendln("legend (\"${chartParams.legend}\", \"position\", \"north\");")
        buffer.appendln("title (\"${chartParams.title}\");")

        return buffer.toString()
    }
}

data class Measurement(val chartName: String, val plotTitle: String, val function: (Graph) -> Double, val xAxisLabel: String, val yAxisLabel: String)

private fun calculateMetrics(graphs: Sequence<Graph>, measurements: Sequence<Measurement>, metricConfig: MetricConfig) {

    val plotXPosition = measurements.count() / 2
    val plotYPosition = plotXPosition + 1

    var metrics = sequenceOf<Metric>()

    measurements.forEachIndexed { i, measurement ->
        var xValues = emptyArray<Int>()
        var yValues = emptyArray<Double>()
        graphs.forEachIndexed { r, graph ->
            xValues = xValues.plus(r + 1)
            val element = java.math.BigDecimal(measurement.function(graph)).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
            yValues = yValues.plus(element)
        }
        val subPlot = SubPlot(plotXPosition, plotYPosition, i + 1)
        metrics = metrics.plus(Metric(ChartData(xValues, yValues), ChartParams(measurement.xAxisLabel, measurement.yAxisLabel, measurement.chartName, measurement.plotTitle), subPlot))
    }
    org.slug.metrics.printMetrics(metricConfig, metrics)

}

private fun printMetrics(metricConfig: MetricConfig, metrics: Sequence<Metric>) {

    val outputPath = java.io.File(metricConfig.outputDirectory + java.io.File.separator + metricConfig.metricsDirectory)
    if (!outputPath.exists()) outputPath.mkdirs()
    val joinToString = metrics.joinToString("\n", "\n", "\n")
    Files.write(java.nio.file.Paths.get(outputPath.path + java.io.File.separator + "metrics.m"), joinToString.toByteArray())
}

fun measurements(graphs: Sequence<Graph>, metricConfig: MetricConfig) {
    val densityMeasure = Measurement("Density", "Density", Toolkit::density, "Graph Id", "Density")
    val averageDegreeMeasure = Measurement("Average Degree", "Average Degree", Toolkit::averageDegree, "Graph Id", "Average Degree")
    val averageDegreeDeviation = Measurement("Average Degree Deviation", "Average Degree Deviation", Toolkit::degreeAverageDeviation, "Graph Id", "Average Degree Deviation")
    val nodeCount = Measurement("Node Count", "Node Spread", ::nodeCount, "Graph Id", "Nodes")
    val edgeCount = Measurement("Edge Count", "Edge Spread", ::edgeCount, "Graph Id", "Edges")
    val diameter = Measurement("Diameter", "Diameter", Toolkit::diameter, "Graph Id", "Diameter")

    val measurements = sequenceOf(densityMeasure, averageDegreeMeasure, averageDegreeDeviation, nodeCount, edgeCount, diameter)

    calculateMetrics(graphs, measurements, metricConfig)

}

fun nodeCount(graph: Graph): Double = graph.nodeCount.toDouble()
fun edgeCount(graph: Graph): Double = graph.edgeCount.toDouble()
