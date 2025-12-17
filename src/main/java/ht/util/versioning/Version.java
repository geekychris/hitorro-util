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
package ht.util.versioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.Env;
import ht.util.core.GenericKeyValue;
import ht.util.core.string.Fmt;
import ht.util.json.JSONUtil;
import ht.util.json.keys.propaccess.PropaccessError;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 3:13:16 PM Wrapper around
 * the version information of the binary release.
 */
public class Version {
    private JsonNode node;

    public Version() {

        // now put some vm and other info
        try {
            node = JVSProperties.getProperties().get("build");
            Env.addVMPropsToMap(JVSProperties.getProperties());
        } catch (PropaccessError propaccessError) {

        }
    }

    public static final String getFullVersionLabel() {
        try {
            return Fmt.S("%s.%s", JVSProperties.getProperties().getString("build.version"),
                    JVSProperties.getProperties().getString("build.number"));
        } catch (PropaccessError propaccessError) {
            return null;
        }
    }

    public List<GenericKeyValue> getValues() {
        List<GenericKeyValue> list = new ArrayList<GenericKeyValue>();
        getValues(list);
        return list;
    }

    public void getValues(List<GenericKeyValue> list) {
        String keys[] = new String[node.size()];
        JsonNode values[] = new JsonNode[node.size()];
        JSONUtil.populateKeyValue((ObjectNode) node, keys, values);
        for (int i = 0; i < keys.length; i++) {
            list.add(new GenericKeyValue(keys[i], values[i]));
        }
    }

}
