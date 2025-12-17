package ht.util.core.thread.farm.jobfarm;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple listFiles based container
 */
public class ListJobResultContainer<RESULT> implements JobResultContainer<RESULT> {
    private List<RESULT> list = new ArrayList();
    private int count = 0;

    public void add(RESULT res) {
        count++;
        if (shouldAdd(res)) {
            list.add(res);
        }
    }

    public int getProcessedCount() {
        return count;
    }

    public List<RESULT> getResults() {
        return list;
    }

    /**
     * overide if you wish to filter
     *
     * @param res
     * @return
     */
    public boolean shouldAdd(RESULT res) {
        return true;
    }
}
