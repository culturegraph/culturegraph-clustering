package org.culturegraph.clustering.graph.plugin;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class GraphMLExporterTest {

    private Charset utf8 = StandardCharsets.UTF_8;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void createGraphML() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter br = new BufferedWriter(writer)) {

            GraphMLExporter exporter = new GraphMLExporter(br);
            exporter.start();
            exporter.addNode("n" + 1, "a", 1);
            exporter.addNode("n" + 2, "b", 1);
            exporter.addNode("n" + 3, "c", 2);
            exporter.addEdge(1, "n" + 1, "n" + 2, 1);
            exporter.end();
        }
        outputStream.close();

        String actual = outputStream.toString(utf8.name());

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +
                "<key id=\"d0\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n" +
                "<key id=\"d1\" for=\"all\" attr.name=\"component\" attr.type=\"int\"/>\n" +
                "<graph id=\"G\" edgedefault=\"undirected\" parse.order=\"nodesfirst\">\n" +
                "<node id=\"n1\"><data key=\"d0\">a</data><data key=\"d1\">1</data></node>\n" +
                "<node id=\"n2\"><data key=\"d0\">b</data><data key=\"d1\">1</data></node>\n" +
                "<node id=\"n3\"><data key=\"d0\">c</data><data key=\"d1\">2</data></node>\n" +
                "<edge id=\"e1\" source=\"n1\" target=\"n2\"><data key=\"d1\">1</data></edge>\n" +
                "</graph>\n" +
                "</graphml>\n";

        assertEquals(actual, expected);
    }
}