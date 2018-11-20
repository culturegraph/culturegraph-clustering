package org.culturegraph.clustering.graph.core.abstractentity;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

/**
 * A container class for a CRS matrix.
 */
public class CRSMatrix {

    int[] val;
    private int rows;
    private int columns;
    private int capacity;  // number of non-zero entries
    private int[] colInd;
    private int[] rowPtr;

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public int capacity() {
        return capacity;
    }

    public int[] getVal() {
        return val;
    }

    public int[] getColInd() {
        return colInd;
    }

    public int[] getRowPtr() {
        return rowPtr;
    }

    public CRSMatrix(int rows, int columns, int capacity, int[] val, int[] colInd, int[] rowPtr) {
        this.rows = rows;
        this.columns = columns;
        this.capacity = capacity;
        this.val = val;
        this.colInd = colInd;
        this.rowPtr = rowPtr;
    }

    /**
     * Gets all elements of a row.
     * @param row A row index.
     * @return A list of row elements.
     */
    public TIntList getRow(int row) {
        TIntList result = new TIntArrayList();
        int start = rowPtr[row];
        int end = rowPtr[row+1];
        for (int i = 0; i < end - start; i++) {
            result.add(val[start+i]);
        }
        return result;
    }
}
