package org.culturegraph.clustering.labelencoder.plugin;

import org.culturegraph.clustering.labelencoder.core.entity.CombinedHashcode;
import org.culturegraph.clustering.labelencoder.core.entity.LabelEncoder;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayLabelEncoder implements LabelEncoder
{
    private CombinedHashcode combinedHashcode;
    private long[] hashes;
    private int size;

    public ArrayLabelEncoder(int size)
    {
        this.size = size;
        this.hashes = new long[size];
        this.combinedHashcode = new CombinedHashcode();
    }

    public int size()
    {
        return hashes.length;
    }

    public static ArrayLabelEncoder fromHashIterator(int size, Iterator<Long> hashIterator)
    {
        ArrayLabelEncoder newInstance = new ArrayLabelEncoder(size);

        int idx = 0;
        while (hashIterator.hasNext())
        {
            long hashCode = hashIterator.next();
            newInstance.hashes[idx] = hashCode;
            idx += 1;
        }

        // Sort to support binary search on arrays
        Arrays.sort(newInstance.hashes);

        return newInstance;
    }

    public static ArrayLabelEncoder fromLabelIterator(int size, Iterator<String> labels)
    {
        ArrayLabelEncoder newInstance = new ArrayLabelEncoder(size);

        int idx = 0;
        while (labels.hasNext())
        {
            String label = labels.next();
            long hashCode = newInstance.combinedHashcode.hashCode(label);
            newInstance.hashes[idx] = hashCode;
            idx += 1;
        }

        // Sort to support binary search on arrays
        Arrays.sort(newInstance.hashes);

        return newInstance;
    }

    @Override
    public int transform(String label)
    {
        long hashCode = combinedHashcode.hashCode(label);
        int notFound = -1;
        int i = Arrays.binarySearch(hashes, hashCode);
        return Math.max(notFound, i);
    }
}
