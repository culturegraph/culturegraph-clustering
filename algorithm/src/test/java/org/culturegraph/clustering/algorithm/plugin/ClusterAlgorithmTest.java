package org.culturegraph.clustering.algorithm.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.culturegraph.clustering.io.plugin.CompressedOutputStream;
import org.culturegraph.clustering.io.plugin.DecompressedInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public class ClusterAlgorithmTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File input;
    private File output;

    @Before
    public void setUp() throws Exception
    {
        input = folder.newFile("input");
        output = folder.newFile("output");
    }

    @After
    public void tearDown() throws Exception
    {
        input.delete();
        output.delete();
        folder.delete();
    }

    @Test
    public void apply() throws Exception
    {
        writeFile(input, graph);

        ClusterAlgorithm algorithm = new ClusterAlgorithm(input, output, false, 1);
        algorithm.run();

        String clustering = readFile(output);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + "\n" +
                "<key id=\"d0\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n" +
                "<key id=\"d1\" for=\"all\" attr.name=\"component\" attr.type=\"int\"/>\n" +
                "<graph id=\"G\" edgedefault=\"undirected\" parse.order=\"nodesfirst\">\n" +
                "<node id=\"n0\"><data key=\"d0\">A</data><data key=\"d1\">1</data></node>\n" +
                "<node id=\"n1\"><data key=\"d0\">B</data><data key=\"d1\">2</data></node>\n" +
                "<node id=\"n2\"><data key=\"d0\">C</data><data key=\"d1\">1</data></node>\n" +
                "<edge id=\"e0\" source=\"m0\" target=\"n2\"><data key=\"d1\">1</data></edge>\n" +
                "<edge id=\"e1\" source=\"m2\" target=\"n2\"><data key=\"d1\">1</data></edge>\n" +
                "<edge id=\"e2\" source=\"m0\" target=\"n0\"><data key=\"d1\">1</data></edge>\n" +
                "<edge id=\"e3\" source=\"m1\" target=\"n0\"><data key=\"d1\">1</data></edge>\n" +
                "<edge id=\"e4\" source=\"m2\" target=\"n0\"><data key=\"d1\">1</data></edge>\n" +
                "<edge id=\"e5\" source=\"m3\" target=\"n1\"><data key=\"d1\">2</data></edge>\n" +
                "</graph>\n" +
                "</graphml>\n";

        assertEquals(expected, clustering);
    }

    private final String graph = "A a b c" + "\n" + "B d" + " \n" + "C a c" + "\n";

    private void writeFile(File file, String content) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(CompressedOutputStream.of(file), StandardCharsets.UTF_8)))
        {
            writer.write(content);
        }
    }

    private String readFile(File file) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(DecompressedInputStream.of(file))))
        {
            return reader.lines().collect(Collectors.joining("\n")) + "\n";
        }
    }
}