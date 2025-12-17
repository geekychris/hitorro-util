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
package ht.util.core.modules;


import ht.util.xml.ConfigurationParameter;

import java.util.Map;

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
