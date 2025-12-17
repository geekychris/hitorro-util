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
package com.hitorro.util.propertykeys.complex;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.util.*;

/**
 *
 */
public class ComplexPropertyContext {
    public static final StringProperty TypeKey = new StringProperty("cptype", "", null);
    private static Map<String, ComplexPropertyFactoryInterface> adapterMap = new HashMap();

    public static void add(ComplexPropertyFactoryInterface intf) {
        for (String name : intf.getNames()) {
            adapterMap.put(name.toLowerCase(), intf);
        }
    }

    public static <T extends Object> List<T> getList(String path, String defaultType) throws ComplexPropertiesException, PropaccessError {
        JsonNode keys = JVSProperties.getProperties().get(path);
        List<T> list = new ArrayList<T>();
        Iterator<Map.Entry<String, JsonNode>> iter = keys.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> km = iter.next();
            T t = get(km.getValue(), defaultType, km.getKey());
            list.add(t);
        }
        return list;
    }

    /**
     * Given a path attempt to construct an instance of whatever from the configs
     *
     * @param path
     * @return
     */
    public static <T extends Object> T get(String path, String defaultType) throws ComplexPropertiesException, PropaccessError {
        JsonNode map = JVSProperties.getProperties().get(path);
        return (T) get(map, defaultType, path);
    }

    /**
     * Given a path attempt to construct an instance of whatever from the configs
     *
     * @param path
     * @return
     */
    public static <T extends Object> T get(String path) throws ComplexPropertiesException, PropaccessError {
        JsonNode map = JVSProperties.getProperties().get(path);
        return (T) get(map, null, path);
    }

    public static <T extends Object> T get(JsonNode map, String defaultType, String path) throws ComplexPropertiesException {
        String type = TypeKey.apply(map);
        if (StringUtil.nullOrEmptyOrBlankString(type)) {
            type = defaultType;
        }
        if (StringUtil.nullOrEmptyOrBlankString(type)) {
            throw new NoFactoryFound("No factory type defined for");
        }
        ComplexPropertyFactoryInterface<T> intf = adapterMap.get(type.toLowerCase());
        if (intf == null) {
            throw new NoFactoryFound(Fmt.S("No factory found for %s", type));
        }

        return intf.getInstance(map, type, StringUtil.getLastCanonicalPart(path));
    }
}
