package org.culturegraph.clustering.labelencoder.core.entity;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Test;
import static org.junit.Assert.*;

public class SentenceTokenizerTest
{
    @Test
    public void testIteration() throws Exception
    {
        Iterator<String> sentences = Stream.of("Cats and dogs.", "Dogs and cats.").iterator();

        SentenceTokenizer tokens = new SentenceTokenizer(sentences);
        assertTrue(tokens.hasNext());

        assertEquals("Cats", tokens.next());
        assertEquals("and", tokens.next());
        assertEquals("dogs.", tokens.next());

        assertEquals("Dogs", tokens.next());
        assertEquals("and", tokens.next());
        assertEquals("cats.", tokens.next());

        assertFalse(tokens.hasNext());
    }
}