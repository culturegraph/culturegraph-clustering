package org.culturegraph.clustering.io.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedOutputStream
{
    public static OutputStream of(OutputStream outputStream) throws IOException
    {
        if (outputStream instanceof GZIPOutputStream)
        {
            return outputStream;
        }
        else
        {
            final int kb64 = 65536;
            return new GZIPOutputStream(outputStream, kb64);
        }
    }

    public static OutputStream of(File file) throws IOException
    {
        return of(new FileOutputStream(file));
    }
}
