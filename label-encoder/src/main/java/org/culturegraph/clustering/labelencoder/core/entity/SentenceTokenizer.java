package org.culturegraph.clustering.labelencoder.core.entity;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public class SentenceTokenizer implements Iterator<String>
{
    private final Iterator<String> sentences;
    private StringTokenizer whitespaceTokenizer;

    public SentenceTokenizer(Iterator<String> sentences)
    {
        this.sentences = sentences;
    }

    @Override
    public boolean hasNext()
    {
        if (sentences.hasNext())
        {
            if (whitespaceTokenizer == null || !whitespaceTokenizer.hasMoreTokens())
            {
                whitespaceTokenizer = new StringTokenizer(sentences.next(), " ");
            }
        }

        return (sentences.hasNext() || whitespaceTokenizer.hasMoreTokens());
    }

    @Override
    public String next()
    {
        if (!whitespaceTokenizer.hasMoreTokens())
        {
            if (sentences.hasNext())
            {
                whitespaceTokenizer = new StringTokenizer(sentences.next(), " ");
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        return whitespaceTokenizer.nextToken();
    }
}
