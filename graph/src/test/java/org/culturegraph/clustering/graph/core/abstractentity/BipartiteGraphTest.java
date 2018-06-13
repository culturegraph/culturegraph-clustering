package org.culturegraph.clustering.graph.core.abstractentity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BipartiteGraphTest {
    @Test
    public void fromAdjacencyList() throws Exception
    {
        BipartiteGraph graph = loadGraph(simple);

        assertEquals(4, graph.getRows());
        assertEquals(5, graph.getColumns());
        assertEquals(7, graph.getCapacity());

        LinkedCRSMatrix matrix = graph.getMatrix();

        int[] expectedVal = {1, 1, 2, 3, 3, 4, 4};
        assertArrayEquals("val", expectedVal, matrix.getVal());

        int[] expectedRowPtr = {0, 2, 3, 5, 7};
        assertArrayEquals("rowPtr", expectedRowPtr, matrix.getRowPtr());

        int[] expectedColId = {0, 1, 2, 0, 3, 1, 4};
        assertArrayEquals("colId", expectedColId, matrix.getColInd());

        int[] expectedNext = {3, 5, 2, 0, 4, 1, 6};
        assertArrayEquals("next", expectedNext, matrix.getColumnNextLink());
    }

    @Test
    public void breathFirstSearchFromTop() throws  Exception
    {
        BipartiteGraph graph = loadGraph(simple);

        boolean[] actual = new boolean[4];
        for (int elem: graph.breathFirstSearch(0).toArray())
        {
            actual[elem] = true;
        }

        boolean[] expected = {true, false, true, true};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void breathFirstSearchFromMiddle() throws  Exception
    {
        BipartiteGraph graph = loadGraph(simple);

        boolean[] actual = new boolean[4];
        for (int elem: graph.breathFirstSearch(2).toArray())
        {
            actual[elem] = true;
        }

        boolean[] expected = {true, false, true, true};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void breathFirstSearchFromBottom() throws  Exception
    {
        BipartiteGraph graph = loadGraph(simple);

        boolean[] actual = new boolean[4];
        for (int elem: graph.breathFirstSearch(3).toArray())
        {
            actual[elem] = true;
        }

        boolean[] expected = {true, false, true, true};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void breathFirstSearchOnARingGraph() throws Exception
    {
        BipartiteGraph graph = loadGraph(ring);

        for (int i = 0; i < graph.getRows(); i++)
        {
            graph.breathFirstSearch(i);
        }
    }

    @Test
    public void breathFirstSearchOnAStartGraph() throws Exception
    {
        BipartiteGraph graph = loadGraph(star);

        for (int i = 0; i < graph.getRows(); i++)
        {
            graph.breathFirstSearch(i);
        }
    }

    @Test
    public void breathFirstSearchOnACliqueGraph() throws Exception
    {
        BipartiteGraph graph = loadGraph(clique);

        for (int i = 0; i < graph.getRows(); i++)
        {
            graph.breathFirstSearch(i);
        }
    }

    @Test
    public void findAllConnectedComponents() throws Exception {
        BipartiteGraph graph = loadGraph(simple);
        int[] actual = graph.findAllConnectedComponents();
        int[] expected = {1,2,1,1};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void findAllConnectedComponentsLimitedToSizeOne() throws Exception {
        BipartiteGraph graph = loadGraph(simple);
        int[] actual = graph.findAllConnectedComponents(1);
        int[] expected = {1,2,1,1};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void findAllConnectedComponentsLimitedToSizeTwo() throws Exception {
        BipartiteGraph graph = loadGraph(simple);
        int[] actual = graph.findAllConnectedComponents(2);
        int[] expected = {1,-1,1,1};
        assertArrayEquals(expected, actual);
    }

    private BipartiteGraph loadGraph(Path input) throws Exception {
        File fin = input.toFile();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fin))))
        {
            Iterator<String> lines = br.lines().iterator();
            return BipartiteGraph.fromAdjacencyList(lines);
        }
    }

    final private Path data = Paths.get("src\\test\\resources");
    private Path clique = data.resolve("graphs/clique.txt");
    private Path ring = data.resolve("graphs/ring.txt");
    private Path star = data.resolve("graphs/star.txt");
    private Path simple = data.resolve("graphs/random.txt");
}