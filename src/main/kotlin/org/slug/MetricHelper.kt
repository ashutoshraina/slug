package org.slug

import org.graphstream.algorithm.measure.*
import org.graphstream.graph.Graph

fun plotMetric(graphs: Sequence<Graph>, chartName: String, plotTitle: String, measurement: (Graph) -> Double, yAxisLabel: String) {
    val measure = ChartSeries2DMeasure(chartName)
    (0..graphs.count() - 1).forEach { r ->
        measure.addValue((r + 1).toDouble() * 10, measurement(graphs.elementAt(r)))
    }

    val params = ChartMeasure.PlotParameters()
    params.title = plotTitle
    params.type = ChartMeasure.PlotType.LINE
    params.xAxisLabel = "Graph Id"
    params.yAxisLabel = yAxisLabel
    measure.plot(params)
}