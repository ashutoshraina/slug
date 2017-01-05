package org.slug

import org.graphstream.graph.Graph
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP


class Metric<T>(val xValues: Array<Int>, val yValues: Array<T>, val xLabel: String, val yLabel: String, val legend: String, val title: String) {
    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.appendln(xValues.joinToString(",", "x = [", "];"))
        buffer.appendln(yValues.joinToString(",", "y = [", "];"))
        buffer.appendln("plot (x,y);")
        buffer.appendln("xlabel (\"$xLabel\");")
        buffer.appendln("ylabel (\"$yLabel\");")
        buffer.appendln("legend (\"$legend\", \"position\", \"north\");")
        buffer.appendln("title (\"$title\");")

        return buffer.toString()
    }
}

fun writeMetrics(graphs: Sequence<Graph>, chartName: String, plotTitle: String, measurement: (Graph) -> Double, yAxisLabel: String, outputDirectory: String, metricsDirectory: String) {

    var xValues = emptyArray<Int>()
    var yValues = emptyArray<Double>()
    (0..graphs.count() - 1).forEach { r ->
        xValues = xValues.plus(r + 1)
        val element = BigDecimal(measurement(graphs.elementAt(r))).setScale(2, HALF_UP).toDouble()
        yValues = yValues.plus(element)
    }
    val metric = Metric(xValues, yValues, "Graph Id", yAxisLabel, plotTitle, chartName)
    printMetrics(outputDirectory, metricsDirectory, metric)
}

fun writeMetrics(graphs: Sequence<Graph>, chartName: String, plotTitle: String, measurement: (Graph) -> Double, yAxisLabel: String) {
    writeMetrics(graphs, chartName, plotTitle, measurement, yAxisLabel, "samples", "metrics")
}

fun printMetrics(outputDirectory: String, metricsDirectory: String, metric: Metric<Double>) {

    val outputPath = File(outputDirectory + File.separator + metricsDirectory)
    if (!outputPath.exists()) outputPath.mkdirs()
    val printStream = PrintStream(FileOutputStream(outputPath.path + File.separator + "${metric.legend}.m"))
    printStream.use({ out -> out.print(metric.toString()) })
}