package org.culturegraph.clustering.graph.core.abstractentity;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import gnu.trove.set.TIntSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class BipartiteGraphTest {

    @Test
    public void iterateConnectedComponentsForClique() throws Exception {
        BipartiteGraph graph = loadGraph(clique);

        Iterator<TIntSet> components = graph.iterator();

        assertTrue(components.hasNext());

        TIntSet component = components.next();
        assertEquals(component.size(), 4);
        assertTrue(component.contains(0));
        assertTrue(component.contains(1));
        assertTrue(component.contains(2));
        assertTrue(component.contains(3));

        assertFalse(components.hasNext());

        Writer w = new BufferedWriter(new OutputStreamWriter(System.err));
        graph.exportAsGraphML(w);
        w.close();
    }

    @Test
    public void iterateConnectedComponentsForLine() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        Iterator<TIntSet> components = graph.iterator();

        assertTrue(components.hasNext());

        TIntSet component = components.next();
        assertEquals(component.size(), 4);
        assertTrue(component.contains(0));
        assertTrue(component.contains(1));
        assertTrue(component.contains(2));
        assertTrue(component.contains(3));

        assertFalse(components.hasNext());

        Writer w = new BufferedWriter(new OutputStreamWriter(System.err));
        graph.exportAsGraphML(w);
        w.close();
    }

    @Test
    public void iterateConnectedComponentsForRing() throws Exception {
        BipartiteGraph graph = loadGraph(ring);

        Iterator<TIntSet> components = graph.iterator();

        assertTrue(components.hasNext());

        TIntSet component = components.next();
        assertEquals(component.size(), 4);
        assertTrue(component.contains(0));
        assertTrue(component.contains(1));
        assertTrue(component.contains(2));
        assertTrue(component.contains(3));

        assertFalse(components.hasNext());

        Writer w = new BufferedWriter(new OutputStreamWriter(System.err));
        graph.exportAsGraphML(w);
        w.close();
    }

    @Test
    public void iterateConnectedComponentsForStar() throws Exception {
        BipartiteGraph graph = loadGraph(star);

        Iterator<TIntSet> components = graph.iterator();

        assertTrue(components.hasNext());

        TIntSet component = components.next();
        assertEquals(component.size(), 4);
        assertTrue(component.contains(0));
        assertTrue(component.contains(1));
        assertTrue(component.contains(2));
        assertTrue(component.contains(3));

        assertFalse(components.hasNext());

        Writer w = new BufferedWriter(new OutputStreamWriter(System.err));
        graph.exportAsGraphML(w);
        w.close();
    }

    @Test
    public void iterateConnectedComponentsForRandom() throws Exception {
        BipartiteGraph graph = loadGraph(simple);

        Iterator<TIntSet> components = graph.iterator();

        assertTrue(components.hasNext());
        TIntSet component1 = components.next();
        assertEquals(component1.size(), 3);
        assertTrue(component1.contains(0));
        assertTrue(component1.contains(2));
        assertTrue(component1.contains(3));

        assertTrue(components.hasNext());
        TIntSet component2 = components.next();
        assertEquals(component2.size(), 1);
        assertTrue(component2.contains(1));

        assertFalse(components.hasNext());

        Writer w = new BufferedWriter(new OutputStreamWriter(System.err));
        graph.exportAsGraphML(w);
        w.close();
    }

    @Test
    public void stopBfsAfterDepthOfZero() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        int startRow = 0;

        TIntSet bfs = graph.breadthFirstSearch(startRow, 0);

        List<Integer> actual = Arrays.stream(bfs.toArray())
                .boxed()
                .collect(Collectors.toList());

        assertEquals(actual.size(), 1);
        assertTrue(actual.contains(0));
    }

    @Test
    public void stopBfsAfterDepthOfOne() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        int startRow = 0;

        TIntSet bfs = graph.breadthFirstSearch(startRow, 1);

        List<Integer> actual = Arrays.stream(bfs.toArray())
                .boxed()
                .collect(Collectors.toList());

        assertEquals(actual.size(), 2);
        assertTrue(actual.contains(0));
        assertTrue(actual.contains(1));
    }

    @Test
    public void stopBfsAfterDepthOfTwo() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        int startRow = 0;

        TIntSet bfs = graph.breadthFirstSearch(startRow, 2);

        List<Integer> actual = Arrays.stream(bfs.toArray())
                .boxed()
                .collect(Collectors.toList());

        assertEquals(actual.size(), 3);
        assertTrue(actual.contains(0));
        assertTrue(actual.contains(1));
        assertTrue(actual.contains(2));
    }

    @Test
    public void stopBfsAfterDepthOfThree() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        int startRow = 0;

        TIntSet bfs = graph.breadthFirstSearch(startRow, 3);

        List<Integer> actual = Arrays.stream(bfs.toArray())
                .boxed()
                .collect(Collectors.toList());

        assertEquals(actual.size(), 4);
        assertTrue(actual.contains(0));
        assertTrue(actual.contains(1));
        assertTrue(actual.contains(2));
        assertTrue(actual.contains(3));
    }

    @Test
    public void bfsShouldReturnComponentIfDepthIsLargerThanGraphDepth() throws Exception {
        BipartiteGraph graph = loadGraph(line);

        int startRow = 0;

        TIntSet bfs = graph.breadthFirstSearch(startRow, 10);

        List<Integer> actual = Arrays.stream(bfs.toArray())
                .boxed()
                .collect(Collectors.toList());

        assertEquals(actual.size(), 4);
        assertTrue(actual.contains(0));
        assertTrue(actual.contains(1));
        assertTrue(actual.contains(2));
        assertTrue(actual.contains(3));
    }

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
        for (int elem: graph.breadthFirstSearch(0).toArray())
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
        for (int elem: graph.breadthFirstSearch(2).toArray())
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
        for (int elem: graph.breadthFirstSearch(3).toArray())
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
            graph.breadthFirstSearch(i);
        }
    }

    @Test
    public void breathFirstSearchOnAStartGraph() throws Exception
    {
        BipartiteGraph graph = loadGraph(star);

        for (int i = 0; i < graph.getRows(); i++)
        {
            graph.breadthFirstSearch(i);
        }
    }

    @Test
    public void breathFirstSearchOnACliqueGraph() throws Exception
    {
        BipartiteGraph graph = loadGraph(clique);

        for (int i = 0; i < graph.getRows(); i++)
        {
            graph.breadthFirstSearch(i);
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

    final private Path data = Paths.get("src/test/resources");
    private Path clique = data.resolve("graphs/clique.txt");
    private Path ring = data.resolve("graphs/ring.txt");
    private Path star = data.resolve("graphs/star.txt");
    private Path simple = data.resolve("graphs/random.txt");
    private Path line = data.resolve("graphs/line.txt");
}