package org.culturegraph.clustering.algorithm.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.culturegraph.clustering.algorithm.core.Step;
import org.culturegraph.clustering.algorithm.core.StepException;

public class CountLinesStep implements Step<Long>
{
    private InputStream inputStream;

    public CountLinesStep(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    @Override
    public Long apply() throws StepException
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {
            return br.lines().count();
        }
        catch (IOException e)
        {
            throw  new StepException("Could not count lines.", e);
        }
    }
}
