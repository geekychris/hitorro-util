/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core.modules;


import ht.util.xml.ConfigurationParameter;

import java.util.Map;

/**
 * The most basic definition of a module - can be configured.
 */
public interface SimpleModule {
    /**
     * Obtain information about configuration parameters.
     *
     * @return an array of configuration parameters, which will determine the parameters passed into the
     * <code>config()</code) call.  A null return is equivalent to an empty return, meaning no parameters are
     * expected.
     */
    ConfigurationParameter[] getConfigurationParameters();

    /**
     * Configure an instance once it has been constructed. We hand the module instances of the configuration parameters
     * in a Map which has parameter names as keys and ConfigurationParameter instances as values.  The caller guarantees
     * that all required parameters are present, that no extra parameters are present, and that all parameters have the
     * correct type.
     *
     * @param parameters The parameters
     * @return true if the configuration was successful, false on failure
     */
    boolean configure(Map parameters);
}
