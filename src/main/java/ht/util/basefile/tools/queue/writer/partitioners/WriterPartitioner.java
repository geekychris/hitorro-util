package ht.util.basefile.tools.queue.writer.partitioners;

import com.fasterxml.jackson.databind.JsonNode;

public interface WriterPartitioner<T> {

    void initForPass(JsonNode params);

    long getPartitionSequenceNumber();

    boolean isWithinFileRange();

    /**
     * reset the state of the partitioner for unit test reasons.
     */
    void reset();

    T getCurrent();

    void setCurrent(T o);

    /**
     * Determines even if we have an item to write a file for.
     *
     * @return
     */
    boolean hasCurrent();

    /**
     * Used to define the lower upper bound of objects that should reside within the current block file
     */
    void setPartitionBoundaries();
}
