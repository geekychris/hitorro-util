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
package com.hitorro.util.versioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * the version information of the binary release.
 */
public class Version {
    private JsonNode node;

    public Version() {

        // now put some vm and other info
        node = GlobalProperties.getProperties().get("build");
        JsonNode props = GlobalProperties.getProperties();
        if (props instanceof ObjectNode) {
            Env.addVMPropsToMap((ObjectNode) props);
        }
    }

    public static final String getFullVersionLabel() {
        return Fmt.S("%s.%s", GlobalProperties.getString("build.version"),
                GlobalProperties.getString("build.number"));
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
