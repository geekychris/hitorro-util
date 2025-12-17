package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Allow ORing of multiple Name filters.  Optimized to return true on the first subordinate that returns true.
 * <p/>
 * Requires three phases of use:
 * <p/>
 * 1) put 2) use in accept logic of a file.listFiles(FilenameFilter)
 *
 * @author chris
 */
public class OrCollection extends LogicalCollection {

    public OrCollection(FilenameFilter... filters) {
        super(filters);
    }


    @Override
    public boolean accept(File dir, String name) {

        if (m_tempFilters != null) {
            finalizeArray();
        }
        for (int i = 0; i < m_filters.length; i++) {
            if (m_filters[i].accept(dir, name)) {
                // one of the filters passed, since its an OR operation
                // this whole collection succeeds.
                return true;
            }
        }
        return false;
    }

}
