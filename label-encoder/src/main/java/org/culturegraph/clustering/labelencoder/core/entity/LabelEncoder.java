package org.culturegraph.clustering.labelencoder.core.entity;

public interface LabelEncoder
{
    /**
     * Transform labels to normalized encoding.
     */
    int transform(String label);
}
