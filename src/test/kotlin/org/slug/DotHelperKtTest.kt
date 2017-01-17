package org.slug

import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class DotHelperKtTest {

  @Test
  fun generateDotFile() {
    val graph = SingleGraph("test")
    graph.addNode<Node>("foo")
    graph.addNode<Node>("bar")
    graph.addEdge<Edge>("123", "foo", "bar")
    val byteArrayOutputStream = ByteArrayOutputStream()
    org.slug.output.generateDotFile(graph, PrintStream(byteArrayOutputStream))
    val result = byteArrayOutputStream.toString()
    assertTrue(result.contains("foo->bar"))
  }

}
