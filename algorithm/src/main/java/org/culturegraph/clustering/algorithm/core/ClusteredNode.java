package org.culturegraph.clustering.algorithm.core;

public class ClusteredNode extends Node
{
    private int cluster;

    public ClusteredNode(String label, int cluster)
    {
        super(label);
        this.cluster = cluster;
    }

    public int getCluster()
    {
        return cluster;
    }

    @Override
    public String asString()
    {
        return this.getLabel() + " " + cluster;
    }
}
