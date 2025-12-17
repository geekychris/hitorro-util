/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core.modules;

import ht.util.core.Log;
import ht.util.xml.ConfigurationParameter;
import ht.util.xml.SimpleDOMNode;

import java.util.Map;

/**
 * Loads a Module and configures it. The information for the module is take from an XML node
 */
public class ModuleLoader {
    //--------------------------------------------------------------------------

    /**
     * Load a Module, based on an XML node
     *
     * @param moduleNode The XML input for the module
     * @return a configured SimpleModule, or null if there is a problem
     */
    public static SimpleModule load(SimpleDOMNode moduleNode) {
        if (moduleNode == null) {
            return null;
        }

        // Make an instance
        SimpleModule sm = getInstance(moduleNode);

        // ask the instance for its configuration parameters
        ConfigurationParameter[] paramMeta = sm.getConfigurationParameters();
        // and load up those parameters
        Map parameters = ConfigurationParameter.parseParameters(moduleNode, paramMeta);
        if (parameters == null) {
            // some problem parsing
            return null;
        }

        // configure the instance
        boolean configured = sm.configure(parameters);

        // we're done
        return configured ? sm : null;
    }

    //--------------------------------------------------------------------------
    private static SimpleModule getInstance(SimpleDOMNode modNode) {
        // there should be a single class node
        SimpleDOMNode[] classNode = modNode.getChildren("class");
        if (classNode == null || classNode.length != 1) {
            Log.util.error("getNewInstance there should be one and only one class node");
            return null;
        }

        SimpleModule retVal = null;
        try {
            Class mclass = Class.forName(classNode[0].getAttributeString(ConfigurationParameter.Name));
            if (mclass != null) {
                retVal = (SimpleModule) mclass.newInstance();
            }
        } catch (Exception exc) {
            Log.util.error("getNewInstance Trouble instantiating %s", exc);
        }

        return retVal;
    }
}

