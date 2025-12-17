package ht.util.core.thread.farm.jobfarm;

/**
 * The container is used to receive results to be handed back from the JobFarm.execute() method.  It must account for
 * all of the items it received via the put method but does not necessarily need to retain them.
 */
public interface JobResultContainer<RESULT> {
    /**
     * put a result to the container.  Even if you dont put the result to the container, you MUST keep track of how many
     * items you processed as we will not return the container till then.
     *
     * @param res
     */
    void add(RESULT res);

    /**
     * number of items received by this container (not necessarily == to the amount of items held in the container
     *
     * @return
     */
    int getProcessedCount();
}
