package org.slug.output

import org.graphstream.graph.Graph
import org.slug.util.FileHelper

class DisplayHelper {

    fun loadDefaultCSS(): String {
        return loadCSS("style.css")
    }

    fun loadCSS(styleFile : String): String {
       return FileHelper.readFile(styleFile)
    }
}

fun display(graph: Graph) {
        graph.display()
        Thread.sleep(1000)
        graph.addAttribute("ui.screenshot", "samples/" + graph.id + "_screenshot.png")
}
