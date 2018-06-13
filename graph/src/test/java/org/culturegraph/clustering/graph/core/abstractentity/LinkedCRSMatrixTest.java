package org.culturegraph.clustering.graph.core.abstractentity;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class LinkedCRSMatrixTest {

    private LinkedCRSMatrix matrix;

    @Before
    public void setUp()
    {
        int rows = 4;
        int columns = 5;
        int capacity = 7;
        int[] val = {10, 12, 11, 13, 16, 11, 13};
        int[] colInd = {0, 3, 2, 4, 1, 2, 4};
        int[] rowPtr = {0, 2, 4, 5, 7};
        matrix = new LinkedCRSMatrix(rows, columns, capacity, val, colInd, rowPtr);
    }

    @Test
    public void testForwardLinks()
    {
        int[] expected = {0, 1, 5, 6, 4, 2, 3};
        int[] actual = matrix.getColumnNextLink();
        assertArrayEquals(expected, actual);
    }
}