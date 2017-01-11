package org.slug.output

import org.graphstream.graph.Graph
import org.slug.Main
import org.slug.util.FileHelper
import java.io.File

object DisplayHelper {
    private fun loadDefaultCSS(): String {
        return loadCSS("style.css")
    }

    private fun loadCSS(styleFile: String): String {
        return FileHelper.readFile(styleFile)
    }

    fun loadCSSConfig(styleFile: String): String {
        val css = when {
            !styleFile.isNullOrEmpty() -> loadCSS(styleFile)
            else -> loadDefaultCSS()
        }
        return css
    }
}

fun display(graph: Graph) {
    graph.display()
    Thread.sleep(1000)
    graph.addAttribute("ui.screenshot", "samples" + File.separator + graph.id + "_screenshot.png")
}
