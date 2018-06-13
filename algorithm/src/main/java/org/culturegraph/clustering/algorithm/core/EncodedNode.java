package org.culturegraph.clustering.algorithm.core;

public class EncodedNode extends Node
{

    private int id;

    public EncodedNode(String label, int id)
    {
        super(label);
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static EncodedNode parse(String s)
    {
        String[] pair = s.split(" ", 2);
        String label = pair[0];
        String idNumber = pair[1];
        return new EncodedNode(label, Integer.parseInt(idNumber));
    }

    @Override
    public String asString()
    {
        return this.getLabel() + " " + id;
    }
}
