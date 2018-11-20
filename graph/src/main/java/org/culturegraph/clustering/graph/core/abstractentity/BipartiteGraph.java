package org.culturegraph.clustering.graph.core.abstractentity;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.culturegraph.clustering.graph.core.entity.AdjacencyList;
import org.culturegraph.clustering.graph.core.entity.GraphHeader;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

/**
 * Class that represents a bipartite graph.
 *
 * A graph G(V, U, E) is bipartite, if there are two distinct stets of nodes (namely V and U)
 * that only share connections between each other. For all edges (v, u) in E, v in V and u in U.
 */
public class BipartiteGraph {
    /**
     * Number of rows. This correspond to the size of V.
     */
    private int rows;
    /**
     * Number of columns. This corresponds to the size of U.
     */
    private int columns;
    /**
     * Number of non-zero element in the compressed matrix. This corresponds to the size of the edge set E.
     */
    private int capacity;
    /**
     * Compressed row storage.
     */
    private LinkedCRSMatrix matrix;

    public BipartiteGraph(LinkedCRSMatrix matrix) {
        this.rows = matrix.rows();
        this.columns = matrix.columns();
        this.capacity = matrix.capacity();
        this.matrix = matrix;
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

    public LinkedCRSMatrix getMatrix() {
        return matrix;
    }

    /**
     * Creates a bipartite graph for an input file iterator.
     *
     * @param input A file iterator.
     * @return A bipartite graph object.
     */
    static public BipartiteGraph fromAdjacencyList(Iterator<String> input) throws IOException
    {
        // Matrix Properties
        int rows = 0;
        int columns = 0;
        int noOfNonZeroEntries = 0;

        // Sparse Matrix Representation
        int[] val = null, colInd = null, rowPtr = null;

        // Current Capacity / Number of non-zero entries
        int capacity = 0;

        boolean hasHeader = false;
        String commentSymbol = "%";
        String headerSymbol = "#";

        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();

        while (input.hasNext())
        {
            String line = input.next();
            if (line.startsWith(commentSymbol) || line.trim().isEmpty())
            {
                continue;
            }

            if (line.startsWith(headerSymbol))
            {
                GraphHeader header = GraphHeader.parse(line);
                noOfNonZeroEntries = header.getCapacity();
                rows = header.getRows();
                columns = header.getColumns();

                val = new int[noOfNonZeroEntries];
                colInd = new int[noOfNonZeroEntries];
                rowPtr = new int[rows+1];

                hasHeader = true;
                continue;
            }

            if (!hasHeader)
            {
                throw new NoSuchElementException("Missing header. Expected '# <ROWS> <COLUMNS> <NON-ZERO-ENTRIES>'.");
            }

            AdjacencyList adj = AdjacencyList.of(parser.parseLine(line));
            int nodeId = adj.getNode();
            int neighbourhoodSize = adj.getNeighbourhood().length;

            int startWithZero = -1;
            for (int neighbour = 0; neighbour < neighbourhoodSize; neighbour++) {
                val[capacity] = nodeId;
                colInd[capacity] = adj.getNeighbourhood()[neighbour] + startWithZero;
                capacity += 1;
            }

            rowPtr[nodeId] = rowPtr[nodeId + startWithZero] + neighbourhoodSize;
        }

        LinkedCRSMatrix matrix = new LinkedCRSMatrix(rows, columns, noOfNonZeroEntries, val, colInd, rowPtr);
        return new BipartiteGraph(matrix);
    }

    /**
     * Performs a Breath-First-Search (BFS) on a bipartite graph *G(V, U, E)*.
     *
     * @param startNode the node index for a node ( *v* in *V* ), where the BFS starts. This corresponds to the row
     *                  index in the adjacency matrix.
     * @return A set containing the the visited nodes that form a connected component.
     */
    public TIntSet breadthFirstSearch(int startNode)
    {
        int noDepthLimit = -1;
        return this.breadthFirstSearch(startNode, noDepthLimit);
    }

    public TIntSet breadthFirstSearch(int startNode, int maxDepth) {
        TIntSet visitedInNodeSetV = new TIntHashSet(100, 0.75f, -1);

        TIntSet visitedInNodeSetU = new TIntHashSet(100, 0.75f, -1);

        Queue<Integer> queue = new ArrayDeque<>();

        int currentRowIdx;

        int start;

        int offset;

        queue.add(startNode);
        visitedInNodeSetV.add(startNode);

        int depth = 0;
        while (!queue.isEmpty())
        {
            if (maxDepth > -1 && depth >= maxDepth) {
                break;
            }

            // Fetch a row
            currentRowIdx = queue.remove();

            // Go through a row and collect all indices,
            // in all columns that attach to this row.
            start = matrix.getRowPtr()[currentRowIdx];
            offset = matrix.getRowPtr()[currentRowIdx+1] - start;


            boolean isDepthIncreased = false;

            for (int off = 0; off < offset; off++) {
                int[] column = matrix.getColumn(start+off).toArray();

                if (maxDepth > -1 && !isDepthIncreased) {
                    for (int i = 0; i < column.length; i++) {
                        int col = column[i];
                        if (!visitedInNodeSetU.contains(col)) {
                            isDepthIncreased = true;
                            break;
                        }
                    }
                    if (isDepthIncreased) depth += 1;
                }

                for (int columnValue: column)
                {
                    if (visitedInNodeSetU.contains(columnValue))
                    {
                        continue;
                    }

                    int remappedToRowIdx = columnValue - 1;
                    if (!visitedInNodeSetV.contains(remappedToRowIdx))
                    {
                        queue.add(remappedToRowIdx);
                        visitedInNodeSetV.add(remappedToRowIdx);
                    }
                    visitedInNodeSetU.add(columnValue);
                }
            }
        }

        return visitedInNodeSetV;
    }

    /**
     * Runs a BFS search on every node to find all connected components. Already visited nodes are omitted as a start
     * node for a search.
     *
     * @return An array that contains a positive component label `j` for every node *v* in *V* with index `i`.
     */
    public int[] findAllConnectedComponents()
    {
        return this.findAllConnectedComponents(0);
    }

    /**
     * Runs a BFS search on every node to find all connected components. Already visited nodes are omitted as a start
     * node for a search.
     *
     * @param minSize Minimum size of a component. Components smaller than this size are flagged with `-1` in the
     *                output array.
     * @return An array that contains a positive component label `j` for every node *v* in *V* with index `i`.
     */
    public int[] findAllConnectedComponents(int minSize)
    {
        int[] result = new int[rows];

        Arrays.fill(result, -1);

        boolean[] visited = new boolean[rows];

        for (int i = 0; i < visited.length; i++) visited[i] = false;

        int label = 1;

        for (int i = 0; i < rows; i++)
        {
            if (visited[i])
            {
                continue;
            }

            TIntSet bfs = breadthFirstSearch(i);
            if (bfs.size() >= minSize)
            {
                TIntIterator intIterator = bfs.iterator();
                while (intIterator.hasNext())
                {
                    int elem = intIterator.next();
                    result[elem] = label;
                    visited[elem] = true;
                }
                label += 1;
            }
        }
        return result;
    }

    public Iterator<TIntSet> iterator() {
        return new ComponentIterator();
    }

    private class ComponentIterator implements Iterator<TIntSet> {

        private boolean[] visited;
        private int row;

        public ComponentIterator() {
            this.visited= new boolean[rows];
            for (int i = 0; i < visited.length; i++) visited[i] = false;
            this.row = 0;
        }

        @Override
        public boolean hasNext() {
            while (visited[row]) {
                row += 1;
                if (row == rows) break;
            }
            return row < rows;
        }

        @Override
        public TIntSet next() {
            TIntSet bfs = breadthFirstSearch(row);
            TIntIterator intIterator = bfs.iterator();
            while (intIterator.hasNext())
            {
                int elem = intIterator.next();
                visited[elem] = true;
            }
            return bfs;
        }
    }
}
