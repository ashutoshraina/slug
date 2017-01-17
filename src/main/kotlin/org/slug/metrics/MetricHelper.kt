package org.slug.metrics

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph
import java.io.File.separator
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP
import java.nio.file.Files
import java.nio.file.Paths

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

private fun calculateMetrics(graphs: Sequence<Graph>, measurements: Sequence<Measurement>): Sequence<Metric> {

  val plotXPosition = measurements.count() / 2
  val plotYPosition = plotXPosition + 1

  var metrics = sequenceOf<Metric>()

  measurements.forEachIndexed { i, measurement ->
    var xValues = emptyArray<Int>()
    var yValues = emptyArray<Double>()
    graphs.forEachIndexed { r, graph ->
      xValues = xValues.plus(r + 1)
      val element = BigDecimal(measurement.function(graph)).setScale(2, HALF_UP).toDouble()
      yValues = yValues.plus(element)
    }
    val subPlot = SubPlot(plotXPosition, plotYPosition, i + 1)
    metrics = metrics.plus(Metric(ChartData(xValues, yValues), ChartParams(measurement.xAxisLabel, measurement.yAxisLabel, measurement.chartName, measurement.plotTitle), subPlot))
  }

  return metrics
}

fun printMetrics(metrics: Sequence<Metric>, metricConfig: MetricConfig) {

  val outputPath = java.io.File(metricConfig.outputDirectory + separator + metricConfig.metricsDirectory)
  if (!outputPath.exists()) outputPath.mkdirs()
  val joinToString = metrics.joinToString("\n", "\n", "\n")
  Files.write(Paths.get(outputPath.path + separator + "metrics.m"), joinToString.toByteArray())
}

fun measurements(graphs: Sequence<Graph>): Sequence<Metric> {
  val densityMeasure = Measurement("Density", "Density", Toolkit::density, "Graph Id", "Density")
  val averageDegreeMeasure = Measurement("Average Degree", "Average Degree", Toolkit::averageDegree, "Graph Id", "Average Degree")
  val averageDegreeDeviation = Measurement("Average Degree Deviation", "Average Degree Deviation", Toolkit::degreeAverageDeviation, "Graph Id", "Average Degree Deviation")
  val nodeCount = Measurement("Node Count", "Node Spread", ::nodeCount, "Graph Id", "Nodes")
  val edgeCount = Measurement("Edge Count", "Edge Spread", ::edgeCount, "Graph Id", "Edges")
  val diameter = Measurement("Diameter", "Diameter", Toolkit::diameter, "Graph Id", "Diameter")

  val measurements = sequenceOf(densityMeasure, averageDegreeMeasure, averageDegreeDeviation, nodeCount, edgeCount, diameter)

  return calculateMetrics(graphs, measurements)

}

fun combineMetrics(metrics: Sequence<Metric>): Sequence<Metric> {
  val groupedByMetric = metrics.groupBy { it -> it.chartParams.title }
  return groupedByMetric.entries
      .mapIndexed { i, entry ->

        var yValues = emptyArray<Double>()
        var xValues = emptyArray<Int>()
        var count = 0
        entry.value.forEach {
          val chartData = it.chartData
          chartData.yValues.forEach { y ->
            count += 1
            xValues = xValues.plus(count)
            yValues = yValues.plus(y)
          }
        }

        Metric(ChartData(xValues, yValues), ChartParams("GraphId", entry.key, entry.key, entry.key), SubPlot(groupedByMetric.count() / 2, groupedByMetric.count() / 2 + 1, i + 1))
      }.asSequence()
}

fun nodeCount(graph: Graph): Double = graph.nodeCount.toDouble()
fun edgeCount(graph: Graph): Double = graph.edgeCount.toDouble()
