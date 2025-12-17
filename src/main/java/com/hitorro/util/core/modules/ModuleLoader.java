/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.modules;

import com.hitorro.util.core.Log;
import com.hitorro.util.xml.ConfigurationParameter;
import com.hitorro.util.xml.SimpleDOMNode;

import java.util.Map;

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

