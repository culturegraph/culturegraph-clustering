package org.culturegraph.clustering.graph.core.abstractentity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CRSMatrixTest {

    private CRSMatrix crs;

    @Before
    public void setUp()
    {
        int rows = 4;
        int columns = 5;
        int capacity = 7;
        int[] val = {10, 12, 11, 13, 16, 11, 13};
        int[] colInd = {0, 3, 2, 4, 1, 2, 4};
        int[] rowPtr = {0, 2, 4, 5, 7};
        crs = new CRSMatrix(rows, columns, capacity, val, colInd, rowPtr);
    }

    @Test
    public void testGetAllRows()
    {
        List<Integer> actual, expected;

        actual = crs.getRow(0);
        expected = IntStream.of(10, 12).boxed().collect(Collectors.toList());
        assertEquals("Row 0", expected, actual);

        actual = crs.getRow(1);
        expected = IntStream.of(11, 13).boxed().collect(Collectors.toList());
        assertEquals("Row 1", expected, actual);

        actual = crs.getRow(2);
        expected = IntStream.of(16).boxed().collect(Collectors.toList());
        assertEquals("Row 2", expected, actual);

        actual = crs.getRow(3);
        expected = IntStream.of(11, 13).boxed().collect(Collectors.toList());
        assertEquals("Row 3", expected, actual);
    }

}