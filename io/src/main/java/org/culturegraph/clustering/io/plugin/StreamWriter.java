package org.culturegraph.clustering.io.plugin;

import org.culturegraph.clustering.io.core.entity.SimpleWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StreamWriter implements SimpleWriter
{
    private final OutputStream outputStream;
    private final Charset utf8;

    public StreamWriter(OutputStream outputStream)
    {
        this.outputStream = outputStream;
        this.utf8 = StandardCharsets.UTF_8;
    }

    @Override
    public boolean push(String message)
    {
        byte[] bytes = message.getBytes(utf8);
        try
        {
            outputStream.write(bytes);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public boolean newLine()
    {
        return push("\n");
    }
}
