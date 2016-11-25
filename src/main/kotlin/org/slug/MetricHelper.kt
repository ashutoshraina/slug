package org.slug

import org.graphstream.algorithm.measure.ChartMeasure
import org.graphstream.algorithm.measure.ChartSeries2DMeasure
import org.graphstream.graph.Graph

fun plotMetric(graphs: Sequence<Graph>, chartName: String, plotTitle: String, measurement: (graph: Graph) -> Double) {
    val m1 = ChartSeries2DMeasure(chartName)
    (0..graphs.count() - 1).forEach { r ->
        m1.addValue((r + 1).toDouble(), measurement(graphs.elementAt(r)))
    }

    val params = ChartMeasure.PlotParameters()
    params.title = plotTitle
    params.type = ChartMeasure.PlotType.LINE
    m1.plot(params)
}