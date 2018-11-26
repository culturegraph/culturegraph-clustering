package org.culturegraph.clustering.io.plugin;

import org.culturegraph.clustering.io.core.entity.SimpleWriter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StreamWriterTest
{

    @Test
    public void push() throws Exception
    {
        String message = "Hello!";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SimpleWriter writer = new StreamWriter(outputStream);
        writer.push(message);
        writer.newLine();
        outputStream.close();

        String body = outputStream.toString();
        assertEquals(message + "\n", body);
    }

    @Test
    public void pushTwoTimes() throws Exception
    {
        String msg1 = "Hello World!";
        String msg2 = "Goodbye!";

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            StreamWriter writer = new StreamWriter(outputStream);
            writer.push(msg1);
            writer.newLine();
            writer.push(msg2);
            writer.newLine();

            outputStream.flush();

            String body = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

            assertEquals(msg1 + "\n" + msg2 + "\n", body);
        }
    }
}