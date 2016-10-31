package org.slug

import org.graphstream.graph.implementations.SingleGraph

class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val css = CSSLoader().loadCSS()

            customGenerator(css, architectureWithMultipleApplicationsInALayer())
        }

        fun customGenerator(css: String, generator: MicroserviceGenerator) {
            val graph = SingleGraph("First")

            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

            graph.addAttribute("ui.stylesheet", css)

            generator.addSink(graph)

            generator.begin()
            generator.end()

            graph.addAttribute("ui.antialias")
            graph.addAttribute("ui.quality")

            graph.display()
            graph.addAttribute("ui.screenshot", "samples/screenshot.png")
        }
    }
}
