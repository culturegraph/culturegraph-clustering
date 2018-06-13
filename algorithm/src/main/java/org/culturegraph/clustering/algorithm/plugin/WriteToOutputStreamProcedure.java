package org.culturegraph.clustering.algorithm.plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.culturegraph.clustering.algorithm.core.Procedure;
import org.culturegraph.clustering.algorithm.core.ProcedureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteToOutputStreamProcedure implements Procedure
{
    private static final Logger LOG = LoggerFactory.getLogger(WriteToOutputStreamProcedure.class);

    private Stream<String> source;
    private OutputStream target;

    public WriteToOutputStreamProcedure(Stream<String> source, OutputStream target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public void apply() throws ProcedureException
    {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(target, StandardCharsets.UTF_8))))
        {
            source.forEachOrdered(pw::println);
        }
    }
}
