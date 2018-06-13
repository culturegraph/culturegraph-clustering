package org.culturegraph.clustering.algorithm.plugin;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.culturegraph.clustering.io.plugin.CompressedOutputStream;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import org.culturegraph.clustering.io.plugin.DecompressedInputStream;

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
        String expected = "A 1" + "\n" + "B 2" + "\n" + "C 1" + "\n";

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