package org.culturegraph.clustering.labelencoder.core.entity;

import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;

/**
 * Combines Adler32 and the default HashCode by concat them into a long.
 */
public class CombinedHashcode
{
    private Adler32 checksumAdler32;

    public CombinedHashcode()
    {
        checksumAdler32 = new Adler32();
    }

    public long hashCode(String s)
    {
        int lower = s.hashCode();

        checksumAdler32.reset();
        checksumAdler32.update(s.getBytes(StandardCharsets.UTF_8));
        int upper = (int) checksumAdler32.getValue();

        return hashCode(lower, upper);
    }

    public long hashCode(int lower, int upper)
    {
        return (((long)upper) << 32) | (lower & 0xffffffffL);
    }

    public long decodeUpper(long code)
    {
        return (int)(code >> 32);
    }

    public long decodeLower(long code)
    {
        return (int) code;
    }
}
