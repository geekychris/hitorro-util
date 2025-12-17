package ht.util.io.largedata.compressedstreams.aggregator;

import ht.util.io.largedata.CompressedStreamIO;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 27, 2005 Time: 7:51:26 PM
 */
public abstract class AggregatorTuple implements Comparable<AggregatorTuple>, CompressedStreamIO {
    public abstract int compareTo(final AggregatorTuple aggregatorTuple);

    public abstract void consume(final AggregatorTuple aggregatorTuple);
}
