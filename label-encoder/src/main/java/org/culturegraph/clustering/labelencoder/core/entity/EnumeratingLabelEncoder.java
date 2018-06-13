package org.culturegraph.clustering.labelencoder.core.entity;

public class EnumeratingLabelEncoder implements LabelEncoder
{
    private final int start;
    private int current;

    public EnumeratingLabelEncoder()
    {
        this.start = 0;
        this.current = 0;
    }

    public EnumeratingLabelEncoder(int start)
    {
        this.start = start;
        this.current = start;
    }

    @Override
    public int transform(String label)
    {
        return current++;
    }

    public void reset()
    {
        this.current = start;
    }
}
