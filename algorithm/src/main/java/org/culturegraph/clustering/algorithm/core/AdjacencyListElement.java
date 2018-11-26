package org.culturegraph.clustering.algorithm.core;

import java.util.List;

public class AdjacencyListElement
{
    private Node node;
    private List<Node> neighbourhood;

    public AdjacencyListElement(Node node, List<Node> neighbourhood)
    {
        this.node = node;
        this.neighbourhood = neighbourhood;
    }

    public Node getNode()
    {
        return node;
    }

    public List<Node> getNeighbourhood()
    {
        return neighbourhood;
    }

    public String asString()
    {
        return node.toString() + " " + neighbourhood.toString();
    }

    @Override
    public String toString()
    {
        return asString();
    }
}
