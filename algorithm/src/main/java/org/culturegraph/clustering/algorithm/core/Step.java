package org.culturegraph.clustering.algorithm.core;

public interface Step<T>
{
    T apply() throws StepException;
}
