package org.slug

import org.graphstream.graph.Graph
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

data class SubPlot(val rows : Int, val cols : Int, val position: Int)
data class ChartParams(val xLabel : String, val yLabel : String, val legend : String, val title : String)
data class ChartData(val xValues: Array<Int>, val yValues: Array<Double>)

class Metric(val chartData : ChartData, val chartParams : ChartParams, val subPlot : SubPlot) {
    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.appendln("@subplot($subPlot.rows,$subPlot.cols,$subPlot.position)")
        buffer.appendln(chartData.xValues.joinToString(",", "x = [", "];"))
        buffer.appendln(chartData.yValues.joinToString(",", "y = [", "];"))
        buffer.appendln("plot (x,y);")
        buffer.appendln("xlabel (\"$chartParams.xLabel\");")
        buffer.appendln("ylabel (\"$chartParams.yLabel\");")
        buffer.appendln("legend (\"$chartParams.legend\", \"position\", \"north\");")
        buffer.appendln("title (\"$chartParams.title\");")

        return buffer.toString()
    }
}

class Measurement(val chartName: String, val plotTitle: String, val function: (Graph) -> Double, val xAxisLabel: String, val yAxisLabel: String)

fun writeMetrics(graphs: Sequence<Graph>, measurements: Sequence<Measurement>, outputDirectory: String, metricsDirectory: String) {

    var metrics = emptySequence<Metric>()
    val measurementCount = measurements.count()

    (0..measurementCount - 1).forEach { m ->
        val measurement = measurements.elementAt(m)
        var xValues = emptyArray<Int>()
        var yValues = emptyArray<Double>()
        (0..graphs.count() - 1).forEach { r ->
            val graph = graphs.elementAt(r)
            xValues = xValues.plus(r + 1)
            val element = BigDecimal(measurement.function(graph)).setScale(2, HALF_UP).toDouble()
            yValues = yValues.plus(element)
        }
        val subPlot = SubPlot(measurementCount/2, measurementCount/2 + 1, m +1)
        metrics = metrics.plus(Metric(ChartData(xValues, yValues), ChartParams(measurement.xAxisLabel, measurement.yAxisLabel, measurement.chartName, measurement.plotTitle), subPlot))
    }
    printMetrics(outputDirectory, metricsDirectory, metrics)

}

fun printMetrics(outputDirectory: String, metricsDirectory: String, metrics: Sequence<Metric>) {

    val outputPath = File(outputDirectory + File.separator + metricsDirectory)
    if (!outputPath.exists()) outputPath.mkdirs()
    val printStream = PrintStream(FileOutputStream(outputPath.path + File.separator + "metrics.m"))
    printStream.use({ out -> out.print(metrics.joinToString("\n", "\n", "\n")) })
}