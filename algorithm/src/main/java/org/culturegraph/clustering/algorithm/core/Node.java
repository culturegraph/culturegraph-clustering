package org.culturegraph.clustering.algorithm.core;

public class Node
{
    private final String label;

    public Node(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public EncodedNode encode(int id)
    {
        return new EncodedNode(label, id);
    }

    @Override
    public String toString()
    {
        return asString();
    }

    public String asString()
    {
        return label;
    }
}
