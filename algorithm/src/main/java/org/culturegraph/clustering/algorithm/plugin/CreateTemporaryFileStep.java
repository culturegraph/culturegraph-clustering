package org.culturegraph.clustering.algorithm.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.culturegraph.clustering.algorithm.core.ProcedureException;
import org.culturegraph.clustering.algorithm.core.Step;
import org.culturegraph.clustering.algorithm.core.StepException;

public class CreateTemporaryFileStep implements Step<File>
{
    private String name;

    public CreateTemporaryFileStep(String name)
    {
        this.name = name;
    }

    @Override
    public File apply() throws StepException
    {
        if (name.isEmpty())
        {
            throw new ProcedureException("Empty file id.", null);
        }

        try
        {
            Path temp =  Files.createTempFile(name, ".tmp");
            return temp.toFile();
        }
        catch (IOException e)
        {
            throw new ProcedureException("Could not create temporary file.", e);
        }
    }
}
