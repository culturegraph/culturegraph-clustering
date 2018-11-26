package org.culturegraph.clustering.algorithm.plugin;

import java.util.stream.Stream;

import org.culturegraph.clustering.algorithm.core.Step;
import org.culturegraph.clustering.algorithm.core.StepException;
import org.culturegraph.clustering.labelencoder.core.entity.CombinedHashcode;

import gnu.trove.set.hash.TLongHashSet;

public class HashingStep implements Step<TLongHashSet>
{
    private CombinedHashcode combinedHashcode;
    private Stream<String> stringStream;

    public HashingStep(Stream<String> stringStream)
    {
        this.combinedHashcode = new CombinedHashcode();
        this.stringStream = stringStream;
    }

    @Override
    public TLongHashSet apply()
    {
        TLongHashSet set = new TLongHashSet();
        stringStream.forEachOrdered(s -> {
            long hashCode = combinedHashcode.hashCode(s);
            set.add(hashCode);
        });

        return set;
    }
}
