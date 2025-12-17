package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * base implementation of a logica construct to manage a set of subordinate FilenameFilters.
 *
 * @author chris
 */
public abstract class LogicalCollection implements FilenameFilter {
    protected FilenameFilter m_filters[];
    protected ArrayList<FilenameFilter> m_tempFilters =
            new ArrayList<FilenameFilter>();

    public LogicalCollection(FilenameFilter... filters) {
        m_filters = filters;
        m_tempFilters = null;
    }

    /**
     * Add a subordinate filter to the set of filters.
     *
     * @param filter
     */
    public void addFilter(FilenameFilter filter) {
        m_tempFilters.add(filter);
    }

    /**
     * Private mechanism to get called once we are being invoked for a scan and we need to optimized our data
     * structures.
     */
    protected void finalizeArray() {
        int size = m_tempFilters.size();
        m_filters = new FilenameFilter[size];
        for (int i = 0; i < size; i++) {
            m_filters[i] = m_tempFilters.get(i);
        }
        m_tempFilters = null;
    }

    /**
     * Special accept mechanism implemented by subclass.
     */
    public abstract boolean accept(File dir, String name);

}
