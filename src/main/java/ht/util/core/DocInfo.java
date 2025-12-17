/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core;

import java.io.Serializable;

/**
 * Hold general information about a document
 */

public class DocInfo implements Serializable {
    private String _name;

    //--------------------------------------------------------------------------

    /**
     * Construct an empty set of information
     */
    public DocInfo(String name) {
        _name = name;
    }

    //--------------------------------------------------------------------------
    public String getName() {
        return _name;
    }
}

