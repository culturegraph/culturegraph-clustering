package org.culturegraph.clustering.graph.core.entity;

import org.junit.Test;
import static org.junit.Assert.*;

public class GraphHeaderTest
{

    @Test
    public void parseValidHeader()
    {
        String line = "# 10 5 5";
        GraphHeader header = GraphHeader.parse(line);

        assertEquals(10, header.getRows());
        assertEquals(5, header.getColumns());
        assertEquals(5, header.getCapacity());
    }
}