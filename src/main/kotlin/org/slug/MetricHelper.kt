package org.slug

import org.graphstream.graph.Graph
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP


class Metric(val xValues: Array<Int>, val yValues: Array<Double>, val xLabel: String, val yLabel: String, val legend: String, val title: String, val rows: Int, val cols: Int = 3, val position: Int) {
    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.appendln("@subplot($rows,$cols,$position)")
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
        metrics = metrics.plus(Metric(xValues, yValues, measurement.xAxisLabel, measurement.yAxisLabel, measurement.plotTitle, measurement.chartName, measurementCount / 2, measurementCount / 2 + 1, m + 1))
    }
    printMetrics(outputDirectory, metricsDirectory, metrics)

}

fun printMetrics(outputDirectory: String, metricsDirectory: String, metrics: Sequence<Metric>) {

    val outputPath = File(outputDirectory + File.separator + metricsDirectory)
    if (!outputPath.exists()) outputPath.mkdirs()
    val printStream = PrintStream(FileOutputStream(outputPath.path + File.separator + "metrics.m"))
    printStream.use({ out -> out.print(metrics.joinToString("\n", "\n", "\n")) })
}