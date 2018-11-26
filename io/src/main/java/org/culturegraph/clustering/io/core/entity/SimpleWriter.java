package org.culturegraph.clustering.io.core.entity;

public interface SimpleWriter
{
    boolean push(String message);

    boolean newLine();
}
