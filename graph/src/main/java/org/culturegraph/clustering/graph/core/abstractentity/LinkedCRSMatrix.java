package org.culturegraph.clustering.graph.core.abstractentity;

import java.util.LinkedList;
import java.util.List;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * A compressed matrix (compressed row storage) that uses forward links 
 * to link each element in the same column.
 */
public class LinkedCRSMatrix extends CRSMatrix {
    /**
     * Stores the `colInd`-id for the next element with the same id.
     * A forward link.
     */
    private int[] columnNextLink;

    public int[] getColumnNextLink() {
        return columnNextLink;
    }

    public LinkedCRSMatrix(int rows, int columns, int capacity, int[] val, int[] colInd, int[] rowPtr) {
        super(rows, columns, capacity, val, colInd, rowPtr);
        columnNextLink = ringIndex(colInd);
    }

    /**
     * Gets all elements in the same column.
     * @param colIndIdx A column index.
     * @return A list of all elements in the column.
     */
    public List<Integer> getColumn(int colIndIdx) {
        List<Integer> result = new LinkedList<>();

        int idx = colIndIdx;
        while (true) {
            result.add(val[idx]);
            idx = columnNextLink[idx];
            if (idx == colIndIdx) break;
        }

        return result;
    }

    /**
     * Creates an array of pointers, that point to the next array position with the same value in the input array.
     * @param array Input array.
     * @return A array of pointers. Each pointer, points to the next position with the same value.
     */
    private int[] ringIndex(int[] array) {
        int[] result = new int[array.length];

        // Init *last_position* with the first position for each element
        TIntIntHashMap last_position = new TIntIntHashMap(100_000, 0.75f, -1, -1);
        for (int i = 0; i < array.length; i++) {
            int elem = array[i];
            if (!last_position.contains(elem)) {
                last_position.put(elem, i);
            }
        }

        for (int i = array.length - 1; i >= 0; i--) {
            int elem = array[i];
            result[i] = last_position.get(elem);
            last_position.put(elem, i);
        }

        last_position.clear();

        return result;
    }
}
