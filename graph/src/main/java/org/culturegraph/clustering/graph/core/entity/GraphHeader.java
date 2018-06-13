package org.culturegraph.clustering.graph.core.entity;

import java.util.StringTokenizer;


public class GraphHeader
{

    private int rows;
    private int columns;
    private int capacity;

    public GraphHeader(int rows, int columns, int capacity)
    {
        this.rows = rows;
        this.columns = columns;
        this.capacity = capacity;
    }

    public static GraphHeader parse(String header) {
        String trimmed = header.replace("#", "").trim();
        StringTokenizer tokenizer = new StringTokenizer(trimmed, " ");

        int rows = 0;
        int columns = 0;
        int capacity = 0;

        int idx = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int value = Integer.parseInt(token);
            switch (idx) {
                case 0:  // rows
                    rows = value;
                    break;
                case 1:  // columns
                    columns = value;
                    break;
                case 2:  // capacity
                    capacity = value;
                    break;
                default:
                    throw new IllegalArgumentException("Too many values. Expected 3.");
            }
            idx += 1;
        }

        return new GraphHeader(rows, columns, capacity);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getCapacity() {
        return capacity;
    }
}
