package org.slug

import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class DotHelperKtTest {

    @Test
    fun generateDotFile() {
        val graph = SingleGraph("test")
        graph.addNode<Node>("foo")
        graph.addNode<Node>("bar")
        graph.addEdge<Edge>("123","foo","bar")
        val byteArrayOutputStream = ByteArrayOutputStream()
        org.slug.generateDotFile(graph, PrintStream(byteArrayOutputStream))
        val result = byteArrayOutputStream.toString()
        assertEquals("""
        digraph test {
                size="10"
                style=filled;
                color=blue;
                node [style=filled,color=cyan];
            123

        }""", result)

    }

}