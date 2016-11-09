package org.slug

import org.graphstream.graph.implementations.SingleGraph
import org.slug.output.DisplayHelper
import org.slug.output.generateDotFile


class Main {

    companion object {
       val config = Config.fromConfig("default.properties")

        @JvmStatic fun main(args: Array<String>) {
            val css = DisplayHelper().loadCSS()

            customGenerator(css, simpleArchitecture(),"simple")
            customGenerator(css, simple3Tier(),"simple3Tier")
            customGenerator(css, multipleLinks(),"multipleLinks")
            customGenerator(css, e2e(),"e2e")
            customGenerator(css, e2eMultipleApps(),"e2eMultipleApps")
        }


        fun customGenerator(css: String, generator: MicroserviceGenerator, name: String) {
            val graph = SingleGraph(name)

            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

            graph.addAttribute("ui.stylesheet", css)

            generator.addSink(graph)

            generator.begin()
            generator.end()

            graph.addAttribute("ui.antialias")
            graph.addAttribute("ui.quality")

            if (config.getBooleanProperty("display.swing")) {
                graph.display()
                Thread.sleep(1000)
                graph.addAttribute("ui.screenshot", "samples/" + name + "_screenshot.png")
            }

            if (config.getBooleanProperty("display.dot")) generateDotFile(graph)
        }
    }
}
