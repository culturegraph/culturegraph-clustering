package org.culturegraph.clustering.algorithm.plugin.helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.StringJoiner;

import org.culturegraph.clustering.io.plugin.DecompressedInputStream;

public class FileIO
{
    public static void writeFile(File file, String content) throws IOException
    {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), bytes);
    }

    public static String readFile(File file) throws IOException
    {
        byte[] bytes = Files.readAllBytes(file.toPath());

        InputStream inputStream = DecompressedInputStream.of(new ByteArrayInputStream(bytes));

        StringJoiner joiner = new StringJoiner("\n");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {
            br.lines().forEachOrdered(joiner::add);
        }

        return joiner.toString() + "\n";
    }
}
