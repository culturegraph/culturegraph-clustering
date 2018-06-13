package org.culturegraph.clustering.algorithm.core;

/**
 * Signals a failure in the execution of a step.
 */
public class StepException extends Exception
{
    public StepException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

