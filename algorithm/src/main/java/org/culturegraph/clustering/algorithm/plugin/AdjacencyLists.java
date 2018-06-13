package org.culturegraph.clustering.algorithm.plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.culturegraph.clustering.algorithm.core.AdjacencyListElement;
import org.culturegraph.clustering.algorithm.core.Node;

public class AdjacencyLists
{
    public static Stream<AdjacencyListElement> adjacencyListStream(InputStream inputStream)
    {
        Iterator<AdjacencyListElement> iter = new Iterator<AdjacencyListElement>()
        {
            String nextLine = null;

            final String whitespace = " ";
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            @Override
            public boolean hasNext()
            {
                if (nextLine != null)
                {
                    return true;
                }
                else
                {
                    try
                    {
                        nextLine = reader.readLine();
                        return (nextLine != null);
                    } catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public AdjacencyListElement next()
            {
                if (nextLine != null || hasNext())
                {
                    String line = nextLine;
                    nextLine = null;

                    String[] headAndTail = line.split(whitespace, 2);
                    String head = headAndTail[0];

                    Node node = new Node(head);

                    List<Node> neighbourhood;

                    try
                    {
                        String tail = headAndTail[1];
                        neighbourhood = Arrays.stream(tail.split(whitespace)).map(Node::new).collect(Collectors.toList());
                    } catch (IndexOutOfBoundsException e)
                    {
                        neighbourhood = new ArrayList<>();
                    }

                    return new AdjacencyListElement(node, neighbourhood);
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
