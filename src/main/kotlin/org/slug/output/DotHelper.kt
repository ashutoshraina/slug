package org.slug.output

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

fun generateDotFile(graph: Graph) {

    val printStream = PrintStream(FileOutputStream("samples"+ File.separator +"${graph.id}.dot"))
    generateDotFile(graph, printStream)
}

fun generateDotFile(graph: Graph, outputDirectory : String, dotDirectory : String) {

    val outputPath = File(outputDirectory + File.separator + dotDirectory)
    if(!outputPath.exists()) outputPath.mkdirs()
    val printStream = PrintStream(FileOutputStream(outputPath.path + File.separator + "${graph.id}.dot"))
    generateDotFile(graph, printStream)
}

fun generateDotFile(graph: Graph, printStream: PrintStream) {
    val graphName: String = graph.id
    val builder: StringBuilder = StringBuilder()
    graph.getEachEdge<Edge>().forEach { edge -> builder.append(edge.getSourceNode<Node>().id + "->" + edge.getTargetNode<Node>().id).appendln() }
    val dotContent: String =
            """
        digraph $graphName {
                size="15"
                style=filled;
                color=blue;
                node [style=filled,color=lightblue];
            $builder
        }"""

    printStream.use({ out -> out.print(dotContent) })
}