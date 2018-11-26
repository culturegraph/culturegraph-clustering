package org.culturegraph.clustering.labelencoder.plugin;

import org.culturegraph.clustering.labelencoder.core.entity.CombinedHashcode;
import org.culturegraph.clustering.labelencoder.core.entity.LabelEncoder;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ArrayLabelEncoderTest
{
    private LabelEncoder encoder;

    @Before
    public void setUp()
    {
        Iterator<String> vocab = Stream.of("a", "b", "c").iterator();
        encoder = ArrayLabelEncoder.fromLabelIterator(3, vocab);
    }

    @Test
    public void fromHashIterator()
    {
        CombinedHashcode combinedHashcode = new CombinedHashcode();
        long a = combinedHashcode.hashCode("a");
        long b = combinedHashcode.hashCode("b");
        long c = combinedHashcode.hashCode("c");

        Iterator<Long> hashes = Stream.of(a, b, c).iterator();
        LabelEncoder encoder = ArrayLabelEncoder.fromHashIterator(3, hashes);

        assertEquals(0, encoder.transform("a"));
        assertEquals(1, encoder.transform("b"));
        assertEquals(2, encoder.transform("c"));
        assertEquals(-1, encoder.transform("d"));
    }

    @Test
    public void encodeLabelA()
    {
        long code = encoder.transform("a");
        assertEquals(0, code);
    }

    @Test
    public void encodeLabelB()
    {
        long code = encoder.transform("b");
        assertEquals(1, code);
    }

    @Test
    public void encodeLabelC()
    {
        long code = encoder.transform("c");
        assertEquals(2, code);
    }
    @Test
    public void labelNotFound()
    {
        long code = encoder.transform("notFound");
        assertEquals(-1, code);
    }

}