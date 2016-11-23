package org.slug.output

import org.graphstream.graph.Graph
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slug.Main
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException

class DisplayHelper {
    var logger: Logger? = LoggerFactory.getLogger(javaClass)

    fun loadDefaultCSS(): String {
        return loadCSS("style.css")
    }

    fun loadCSS(styleFile : String): String {
        val EMPTY = ""
        try {
            val classLoader = javaClass.classLoader
            val inputReader = classLoader.getResourceAsStream(styleFile)
            val buffer = StringBuffer()
            val bufferedReader = BufferedReader(InputStreamReader(inputReader, "UTF-8"))
            var character = bufferedReader.read()
            while (character != -1) {
                buffer.append(character.toChar())
                character = bufferedReader.read()
            }
            return buffer.toString()
        } catch (e: UnsupportedEncodingException) {
            return EMPTY
        } catch (e: IOException) {
            logger?.warn("unable to load the css file.")
            return EMPTY
        }
    }
}

fun display(graph: Graph) {
    if (Main.config.getBooleanProperty("display.swing")) {
        graph.display()
        Thread.sleep(1000)
        graph.addAttribute("ui.screenshot", "samples/" + graph.id + "_screenshot.png")
    }
}

fun printDotFile(graph: Graph) {
    if (Main.config.getBooleanProperty("display.dot")) generateDotFile(graph)
}
