package org.culturegraph.clustering.graph.core.entity;

public class AdjacencyList {
    private int node;
    private int[] neighbourhood;

    public AdjacencyList(int node, int[] neighbourhood) {
        this.node = node;
        this.neighbourhood = neighbourhood;
    }

    public static AdjacencyList of(String[] token) {

        int node = Integer.parseInt(token[0]);

        if (node <= 0) throw new IllegalArgumentException("Id needs to be 0 or larger");

        int[] neighbourhood = new int[token.length - 1];
        for (int i = 1; i < token.length; i++) {
            int neighbourhoodId = Integer.parseInt(token[i]);
            if (neighbourhoodId <= 0) throw new IllegalArgumentException("Id needs to be 0 or larger");
            neighbourhood[i-1] = neighbourhoodId;
        }

        return new AdjacencyList(node, neighbourhood);
    }

    public int getNode() {
        return node;
    }

    public int[] getNeighbourhood() {
        return neighbourhood;
    }
}
