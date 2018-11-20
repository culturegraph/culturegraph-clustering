package org.culturegraph.clustering.algorithm.plugin;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.culturegraph.clustering.algorithm.core.AdjacencyListElement;
import org.culturegraph.clustering.algorithm.core.Algorithm;
import org.culturegraph.clustering.algorithm.core.EncodedNode;
import org.culturegraph.clustering.algorithm.core.Node;
import org.culturegraph.clustering.algorithm.core.ProcedureException;
import org.culturegraph.clustering.algorithm.core.StepException;
import org.culturegraph.clustering.graph.core.abstractentity.BipartiteGraph;
import org.culturegraph.clustering.graph.core.abstractentity.LinkedCRSMatrix;
import org.culturegraph.clustering.graph.plugin.GraphMLExporter;
import org.culturegraph.clustering.io.plugin.CompressedOutputStream;
import org.culturegraph.clustering.io.plugin.DecompressedInputStream;
import org.culturegraph.clustering.labelencoder.core.entity.EnumeratingLabelEncoder;
import org.culturegraph.clustering.labelencoder.core.entity.LabelEncoder;
import org.culturegraph.clustering.labelencoder.plugin.ArrayLabelEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterAlgorithm implements Algorithm
{
    private static final Logger LOG = LoggerFactory.getLogger(ClusterAlgorithm.class);

    private final File input;
    private final File output;
    private final boolean compressOutput;

    private TLongHashSet childNodesHashSet;
    private File childNodesHashes, encodedInput, encodedParentNodes;
    private EnumeratingLabelEncoder parentNodeLabelEncoder;
    private LabelEncoder childNodeLabelEncoder;

    private long matrixRows, matrixColumns, matrixNonZeroEntries;
    private BipartiteGraph graph;
    private final int minimumComponentSize;
    private int[] connectedComponents;

    public ClusterAlgorithm(File input, File output)
    {
        this(input, output, true, 1);
    }

    public ClusterAlgorithm(File input, File output, boolean compressOutput, int minimumComponentSize)
    {
        this.input = input;
        this.output = output;
        this.compressOutput = compressOutput;
        this.minimumComponentSize = minimumComponentSize;
    }

    public void run() throws Exception
    {
        countNodesAndCollectChildNodesHashes();

        createLabelEncoderForParentNodes();
        createTableForEncodedParentNodes();
        saveEncodedParentNodesIntoTable();

        saveChildNodeHashes();
        createAndPopulateChildNodeLabelEncoderFromChildNodeHashes();
        deleteChildNodeHashes();

        createEncodedInput();
        appendHeaderToEncodedInput();
        appendEncodedAdjacencyListToEncodedInput();

        freeChildNodeLabelEncoder();
        freeParentNodeLabelEncoder();

        loadEncodedInputIntoGraph();

        deleteEncodedInput();

        computeClustering();

        relabelClusteringOutputWithTableForEncodedParentNodesAndWriteOutput();

        deleteTableForEncodedParentNodes();
    }

    private void countNodesAndCollectChildNodesHashes() throws IOException
    {
        if (LOG.isInfoEnabled()) LOG.info("Counting nodes ...");

        try (InputStream inputStream = DecompressedInputStream.of(input))
        {
            Stream<String> neighbourhoodNodeLabelsStream = AdjacencyLists
                    .adjacencyListStream(inputStream)
                    .peek(adj -> matrixRows += 1)
                    .peek(adj -> matrixNonZeroEntries += adj.getNeighbourhood().size())
                    .map(AdjacencyListElement::getNeighbourhood)
                    .flatMap(List::stream)
                    .map(Node::getLabel);

            childNodesHashSet = new HashingStep(neighbourhoodNodeLabelsStream).apply();
        }

        matrixColumns = childNodesHashSet.size();

        if (LOG.isInfoEnabled()) {
            LOG.info("Parent Nodes: {}", matrixRows);
            LOG.info("Child Nodes: {}", matrixNonZeroEntries);
            LOG.info("Unique Child Nodes: {}", matrixColumns);
            LOG.info("Counting nodes completed.");
        }
    }

    private void saveChildNodeHashes() throws IOException, StepException
    {
        childNodesHashes = new CreateTemporaryFileStep("childNodesHashes").apply();

        if (LOG.isInfoEnabled()) LOG.info("Saving child node hashes.");

        try (OutputStream outputStream = CompressedOutputStream.of(childNodesHashes);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(outputStreamWriter))
        {
            TLongIterator hashIterator = childNodesHashSet.iterator();
            while (hashIterator.hasNext())
            {
                long hash = hashIterator.next();
                writer.write(String.valueOf(hash));
                writer.newLine();
            }
        }

        LOG.info("Saving child node hashes completed.");
    }

    private void deleteChildNodeHashes()
    {
        if (LOG.isInfoEnabled()) LOG.info("Deleting child node hashes.");

        childNodesHashSet.clear();
        if (childNodesHashes.delete()) childNodesHashes.deleteOnExit();

        if (LOG.isInfoEnabled()) LOG.info("Deleting child node hashes completed.");
    }

    private void createLabelEncoderForParentNodes()
    {
        if (LOG.isInfoEnabled()) LOG.info("Creating parent node encoder.");

        parentNodeLabelEncoder = new EnumeratingLabelEncoder(0);

        if (LOG.isInfoEnabled()) LOG.info("Creating parent node encoder completed.");
    }

    private void freeParentNodeLabelEncoder()
    {
        if (LOG.isDebugEnabled()) LOG.debug("Freeing parent node label encoder.");

        parentNodeLabelEncoder = null;
    }

    private void createTableForEncodedParentNodes() throws StepException
    {
        if (LOG.isDebugEnabled()) LOG.debug("Creating file for encoded parent nodes.");

        encodedParentNodes = new CreateTemporaryFileStep("encodedParentNodes").apply();
    }

    private void saveEncodedParentNodesIntoTable() throws IOException, ProcedureException
    {
        if (LOG.isInfoEnabled()) LOG.info("Saving encoded parent nodes.");

        parentNodeLabelEncoder.reset();

        try (InputStream inputStream = DecompressedInputStream.of(input))
        {
            Stream<String> parentNodeLabels = AdjacencyLists.adjacencyListStream(inputStream)
                    .map(AdjacencyListElement::getNode)
                    .map(Node::getLabel)
                    .map(label -> new EncodedNode(label, parentNodeLabelEncoder.transform(label)))
                    .map(EncodedNode::asString);
            new WriteToOutputStreamProcedure(parentNodeLabels, CompressedOutputStream.of(encodedParentNodes)).apply();
        }

        if (LOG.isInfoEnabled()) LOG.info("Saving encoded parent nodes completed.");
    }

    private void createAndPopulateChildNodeLabelEncoderFromChildNodeHashes() throws IOException
    {
        if (LOG.isInfoEnabled()) LOG.info("Creating child node encoder.");

        try (InputStream inputStream = DecompressedInputStream.of(childNodesHashes);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader))
        {
            Iterator<Long> hashes = reader.lines().mapToLong(Long::parseLong).iterator();
            childNodeLabelEncoder = ArrayLabelEncoder.fromHashIterator((int) matrixColumns, hashes);
        }

        if (LOG.isInfoEnabled()) LOG.info("Creating child node encoder completed.");
    }

    private void freeChildNodeLabelEncoder()
    {
        if (LOG.isDebugEnabled()) LOG.debug("Free child node encoder");
        childNodeLabelEncoder = null;
    }

    private void exportEdges(GraphMLExporter exporter, BipartiteGraph graph) throws IOException {
        int edgeId = 0;
        int componentId = 0;

        Iterator<TIntSet> componentIterator = graph.iterator();
        while (componentIterator.hasNext()) {
            componentId += 1;
            TIntSet component = componentIterator.next();
            LinkedCRSMatrix matrix = graph.getMatrix();
            for(TIntIterator iter = component.iterator(); iter.hasNext(); ) {
                int target = iter.next();

                int start = matrix.getRowPtr()[target];
                int offset = matrix.getRowPtr()[target + 1];
                int span = offset - start;
                for (int i = 0; i < span; i++) {
                    int idx = start + i;
                    int source = matrix.getColInd()[idx];
                    exporter.addEdge(edgeId++, "m" + source, "n" + target, componentId);
                }
            }
        }
    }

    private void exportNodes(GraphMLExporter exporter, Iterator<EncodedNode> encodedNodeIterator) throws IOException {
        for (int nodeId = 0; nodeId < connectedComponents.length; nodeId++)
        {
            if (!encodedNodeIterator.hasNext())
            {
                if (LOG.isWarnEnabled()) LOG.warn("Ended prematurely!");
                break;
            }

            EncodedNode encodedNode = encodedNodeIterator.next();
            int clusterId = connectedComponents[nodeId];

            exporter.addNode("n" + encodedNode.getId(), encodedNode.getLabel(), clusterId);
        }
    }

    private void relabelClusteringOutputWithTableForEncodedParentNodesAndWriteOutput() throws IOException
    {
        if (LOG.isInfoEnabled()) LOG.info("Decoding output ...");

        try (InputStream inputStream = DecompressedInputStream.of(encodedParentNodes);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader decodingTableReader = new BufferedReader(inputStreamReader);
             OutputStream outputStream = compressOutput ? CompressedOutputStream.of(output) : new FileOutputStream(output);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter output = new BufferedWriter(outputStreamWriter))
        {
            GraphMLExporter exporter = new GraphMLExporter(output);
            exporter.start();

            Iterator<EncodedNode> encodedNodeIterator = decodingTableReader.lines()
                    .map(EncodedNode::parse)
                    .iterator();

            exportNodes(exporter, encodedNodeIterator);
            connectedComponents = null;
            exportEdges(exporter, graph);
            exporter.end();
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Decoding output completed.");
            LOG.info("Wrote output to \"{}\"", output.getName());
        }
    }

    private void deleteTableForEncodedParentNodes()
    {
        if (LOG.isDebugEnabled()) LOG.debug("Delete decoding table for parent node label encoder.");

        if (!encodedParentNodes.delete()) encodedParentNodes.deleteOnExit();
    }

    private void createEncodedInput() throws StepException
    {
        if (LOG.isInfoEnabled()) LOG.info("Encoding input...");

        encodedInput = new CreateTemporaryFileStep("encodedInput").apply();
    }

    private void appendHeaderToEncodedInput() throws IOException, ProcedureException
    {
        if (LOG.isInfoEnabled()) LOG.info("Adding header ...");

        String header = "# " + matrixRows + " " + matrixColumns + " " + matrixNonZeroEntries;

        if (LOG.isDebugEnabled()) LOG.debug("Encoded input header: {}", header);

        try (OutputStream appendOutputStream = new FileOutputStream(encodedInput, true))
        {
            Stream<String> headerStream = Stream.of(header);
            new WriteToOutputStreamProcedure(headerStream, CompressedOutputStream.of(appendOutputStream)).apply();
        }
    }

    private void appendEncodedAdjacencyListToEncodedInput() throws IOException, StepException
    {
        if (LOG.isInfoEnabled()) LOG.info("Adding body ...");

        parentNodeLabelEncoder.reset();

        Function<AdjacencyListElement, String> encodeAdjacencyList = adj -> {
            int startWithOne = 1;

            Node parentNode = adj.getNode();
            int parentNodeId = parentNodeLabelEncoder.transform(parentNode.getLabel()) + startWithOne;
            EncodedNode encodedParentNode = new EncodedNode(parentNode.getLabel(), parentNodeId);

            List<EncodedNode> encodedChildNodes = adj.getNeighbourhood().stream()
                    .map(Node::getLabel)
                    .map(label -> new EncodedNode(label, startWithOne + childNodeLabelEncoder.transform(label)))
                    .collect(Collectors.toList());

            return Stream.concat(Stream.of(encodedParentNode), encodedChildNodes.stream())
                    .map(EncodedNode::getId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));
        };

        try (OutputStream appendOutputStream = new FileOutputStream(encodedInput, true))
        {
            Stream<String> encodedAdjacencyListStream = AdjacencyLists
                    .adjacencyListStream(DecompressedInputStream.of(input))
                    .map(encodeAdjacencyList);

            new WriteToOutputStreamProcedure(encodedAdjacencyListStream, CompressedOutputStream.of(appendOutputStream)).apply();
        }

        if (LOG.isInfoEnabled()) LOG.info("Encoding input completed.");
    }

    private void deleteEncodedInput()
    {
        if (LOG.isDebugEnabled()) LOG.debug("Deleting encoded input ...");

        encodedInput.delete();
        encodedInput.deleteOnExit();
    }

    private void loadEncodedInputIntoGraph() throws IOException
    {
        if (LOG.isInfoEnabled()) LOG.info("Creating graph instance ...");

        try (InputStream inputStream = DecompressedInputStream.of(encodedInput);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader))
        {
            Iterator<String> encodedInputIterator = bufferedReader.lines()
                    .iterator();
            graph = BipartiteGraph.fromAdjacencyList(encodedInputIterator);
        }

        if (LOG.isInfoEnabled()) LOG.info("Creating graph instance completed.");
    }

    private void computeClustering()
    {
        if (LOG.isInfoEnabled()) LOG.info("Computing clustering ...");

        connectedComponents = graph.findAllConnectedComponents(minimumComponentSize);

        if (LOG.isInfoEnabled()) LOG.info("Computing clustering completed.");
    }
}
