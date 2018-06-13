package org.culturegraph.clustering.algorithm.plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.culturegraph.clustering.algorithm.core.*;
import org.culturegraph.clustering.labelencoder.core.entity.EnumeratingLabelEncoder;
import org.culturegraph.clustering.labelencoder.core.entity.LabelEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.culturegraph.clustering.graph.core.abstractentity.BipartiteGraph;
import org.culturegraph.clustering.labelencoder.plugin.ArrayLabelEncoder;
import org.culturegraph.clustering.io.plugin.CompressedOutputStream;
import org.culturegraph.clustering.io.plugin.DecompressedInputStream;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

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
        this.input = input;
        this.output = output;
        this.compressOutput = true;
        this.minimumComponentSize = 1;
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
        LOG.info("Counting nodes ...");

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

        LOG.info("Parent Nodes: {}", matrixRows);
        LOG.info("Child Nodes: {}", matrixNonZeroEntries);
        LOG.info("Unique Child Nodes: {}", matrixColumns);
        LOG.info("Counting nodes completed.");
    }

    private void saveChildNodeHashes() throws IOException, StepException
    {
        childNodesHashes = new CreateTemporaryFileStep("childNodesHashes").apply();

        LOG.info("Saving child node hashes.");

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
        LOG.info("Deleting child node hashes.");

        childNodesHashSet.clear();
        if (childNodesHashes.delete()) childNodesHashes.deleteOnExit();

        LOG.info("Deleting child node hashes completed.");
    }

    private void createLabelEncoderForParentNodes()
    {
        LOG.info("Creating parent node encoder.");

        parentNodeLabelEncoder = new EnumeratingLabelEncoder(0);

        LOG.info("Creating parent node encoder completed.");
    }

    private void freeParentNodeLabelEncoder()
    {
        LOG.debug("Freeing parent node label encoder.");

        parentNodeLabelEncoder = null;
    }

    private void createTableForEncodedParentNodes() throws StepException
    {
        LOG.debug("Creating file for encoded parent nodes.");

        encodedParentNodes = new CreateTemporaryFileStep("encodedParentNodes").apply();
    }

    private void saveEncodedParentNodesIntoTable() throws IOException, ProcedureException
    {
        LOG.info("Saving encoded parent nodes.");

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

        LOG.info("Saving encoded parent nodes completed.");
    }

    private void createAndPopulateChildNodeLabelEncoderFromChildNodeHashes() throws IOException
    {
        LOG.info("Creating child node encoder.");

        try (InputStream inputStream = DecompressedInputStream.of(childNodesHashes);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader))
        {
            Iterator<Long> hashes = reader.lines().mapToLong(Long::parseLong).iterator();
            childNodeLabelEncoder = ArrayLabelEncoder.fromHashIterator((int) matrixColumns, hashes);
        }

        LOG.info("Creating child node encoder completed.");
    }

    private void freeChildNodeLabelEncoder()
    {
        LOG.debug("Free child node encoder");
        childNodeLabelEncoder = null;
    }

    private void relabelClusteringOutputWithTableForEncodedParentNodesAndWriteOutput() throws IOException
    {
        LOG.info("Decoding output ...");

        try (InputStream inputStream = DecompressedInputStream.of(encodedParentNodes);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader decodingTableReader = new BufferedReader(inputStreamReader);
             OutputStream outputStream = compressOutput ? CompressedOutputStream.of(output) : new FileOutputStream(output);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter output = new BufferedWriter(outputStreamWriter))
        {
            Iterator<EncodedNode> encodedNodeIterator = decodingTableReader.lines()
                    .map(EncodedNode::parse)
                    .iterator();

            for (int nodeId = 0; nodeId < connectedComponents.length; nodeId++)
            {
                if (!encodedNodeIterator.hasNext())
                {
                    LOG.warn("Decoding table ended prematurely!");
                    break;
                }

                int clusterId = connectedComponents[nodeId];

                EncodedNode encodedNode = encodedNodeIterator.next();

                ClusteredNode clusteredNode = new ClusteredNode(encodedNode.getLabel(), clusterId);

                if (nodeId == encodedNode.getId())
                {
                    output.write(clusteredNode.asString());
                    output.newLine();
                }
            }
        }

        LOG.info("Decoding output completed.");

        LOG.info("Wrote output to \"{}\"", output.getName());
    }

    private void deleteTableForEncodedParentNodes()
    {
        LOG.debug("Delete decoding table for parent node label encoder.");

        if (!encodedParentNodes.delete()) encodedParentNodes.deleteOnExit();
    }

    private void createEncodedInput() throws StepException
    {
        LOG.info("Encoding input...");

        encodedInput = new CreateTemporaryFileStep("encodedInput").apply();
    }

    private void appendHeaderToEncodedInput() throws IOException, ProcedureException
    {
        LOG.info("Adding header ...");

        String header = "# " + matrixRows + " " + matrixColumns + " " + matrixNonZeroEntries;

        LOG.debug("Encoded input header: {}", header);

        try (OutputStream appendOutputStream = new FileOutputStream(encodedInput, true))
        {
            Stream<String> headerStream = Stream.of(header);
            new WriteToOutputStreamProcedure(headerStream, CompressedOutputStream.of(appendOutputStream)).apply();
        }
    }

    private void appendEncodedAdjacencyListToEncodedInput() throws IOException, StepException
    {
        LOG.info("Adding body ...");

        parentNodeLabelEncoder.reset();

        Function<AdjacencyListElement, String> encodeAdjacencyList = adj -> {
            int startWithOne = 1;

            Node parentNode = adj.getNode();
            int parentNodeId = parentNodeLabelEncoder.transform(parentNode.getLabel()) + startWithOne;
            EncodedNode encodedParentNode = new EncodedNode(parentNode.getLabel(), parentNodeId);

            List<EncodedNode> encodedChildNodes = adj.getNeighbourhood().stream()
                    .map(Node::getLabel)
                    .map(label -> new EncodedNode(label, childNodeLabelEncoder.transform(label)))
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

        LOG.info("Encoding input completed.");
    }

    private void deleteEncodedInput()
    {
        LOG.debug("Deleting encoded input ...");

        encodedInput.delete();
        encodedInput.deleteOnExit();
    }

    private void loadEncodedInputIntoGraph() throws IOException
    {
        LOG.info("Creating graph instance ...");

        try (InputStream inputStream = DecompressedInputStream.of(encodedInput);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader))
        {
            Iterator<String> encodedInputIterator = bufferedReader.lines().iterator();
            graph = BipartiteGraph.fromAdjacencyList(encodedInputIterator);
        }

        LOG.info("Creating graph instance completed.");
    }

    private void computeClustering()
    {
        LOG.info("Computing clustering ...");

        connectedComponents = graph.findAllConnectedComponents(minimumComponentSize);

        LOG.info("Computing clustering completed.");
    }
}
