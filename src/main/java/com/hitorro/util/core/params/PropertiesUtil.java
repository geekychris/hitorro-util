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
package com.hitorro.util.core.params;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.PropertyParts;
import com.hitorro.util.json.keys.StringProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public class PropertiesUtil {

    public static final String VariableEnd = "}";
    public static final String VariableStart = "${";

    public static final String getProperty(Properties props, String field) {
        return props.getProperty(field);
    }

    public static final String resolveJsonVariable(String value, JsonNode node) {
        if (value == null) {
            return null;
        }
        return resolveJsonVariable(value, true, null, node);
    }

    private static String getString(final JsonNode topArgs, final JsonNode args, final String variable, final StringProperty prop) {
        String substValue;
        if (topArgs != null) {
            prop.setPath(variable);
            substValue = prop.apply(topArgs);
            if (substValue == null) {
                substValue = prop.apply(args);
            }
        } else {
            substValue = prop.apply(args);
        }
        return substValue;
    }

    public static final String getProperty(Properties props, String field, String defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return t;
    }

    public static final int getPropertyAsInt(Properties props, String field, int defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return Integer.parseInt(t);
    }

    public static final long getPropertyAsLong(Properties props, String field, long defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return Long.parseLong(t);
    }

    public static final boolean getPropertyAsBool(Properties props, String field, boolean defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return Constants.getBool(t);
    }

    public static final Boolean getPropertyAsBoolean(Properties props, String field, Boolean defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return Constants.getBoolean(t);
    }

    public static final Integer getPropertyAsInteger(Properties props, String field, Integer defaultValue) {
        String t = getProperty(props, field);
        if (StringUtil.nullOrEmptyString(t)) {
            return defaultValue;
        }
        return Constants.getInteger(Integer.parseInt(t));
    }

    /**
     * Bytes2Props -- Load a new Properties object with byte array b.  b should be a data array conformping to
     * Properties format:
     *
     * @return Properties object or null on error.
     * @see java.util.Properties
     */
    public static Properties Bytes2Props(byte b[]) {
        Properties prv = new Properties();

        try {
            prv.load(new ByteArrayInputStream(b));
        } catch (IOException e) {
            Log.util.warn("Failed to load Properties:  %s", e.toString());
            return null;
        }
        return prv;
    }

    /**
     * String2Props -- Load a new Properties object with String p. 'p' is expected to be a string in a Properties
     * format: "key=value\ncolor = red\nport=33\n# ....\n"
     *
     * @return Properties object.  Or null on error.
     */
    public static Properties String2Props(String p) {
        return Bytes2Props(p.getBytes());
    }

    /**
     * Props2Bytes -- get the output of Properties 'props' as a byte array.
     *
     * @return byte[] or null on error.
     */
    public static byte[] Props2Bytes(Properties props) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        try {
            props.store(bs, null);
        } catch (IOException e) {
            Log.util.warn("Props2Bytes: failed to convertToPdf props: %s", e.toString());
            return null;
        }
        return bs.toByteArray();
    }

    public static JsonNode getJsonNodeFromStringArraysMap(Map<String, String[]> map) {

        ObjectNode on = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            ArrayNode an = JsonNodeFactory.instance.arrayNode();
            for (String s : entry.getValue()) {
                an.add(s);
            }
            on.put(entry.getKey(), an);
        }
        return on;
    }

    public static JsonNode getJsonNodeFromStringMap(Map<String, String> map) {
        ObjectNode on = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            on.put(entry.getKey(), entry.getValue());
        }
        return on;
    }

    public static JsonNode getNode(PropertyParts pp, JsonNode node, boolean createIfMissing) {
        return getNode(pp, node, pp.getParts().length, createIfMissing);
    }

    public static JsonNode getNodeParent(PropertyParts pp, JsonNode node, boolean createIfMissing) {
        if (pp.lastPartIndexed()) {
            return getNode(pp, node, pp.getParts().length - 2, createIfMissing);
        } else {
            return getNode(pp, node, pp.getParts().length - 1, createIfMissing);
        }
    }

    public static ArrayNode getNodeAsArrayCreating(PropertyParts pp, JsonNode node) {
        return getNodeAsArrayCreating(pp, node, false);
    }

    public static ArrayNode getNodeAsArrayCreating(PropertyParts pp, JsonNode node, boolean partsIncludesIndexPosition) {
        JsonNode n;
        if (partsIncludesIndexPosition) {
            n = getNode(pp, node, pp.getParts().length - 1, false);
        } else {
            n = getNode(pp, node, true);
        }
        if (n == null) {
            JsonNode par = getNodeParent(pp, node, true);
            if (par == null) {
                return null;
            }
            n = JsonNodeFactory.instance.arrayNode();
            ((ObjectNode) par).put(pp.getName(), n);
        }
        if (n.isArray()) {
            return (ArrayNode) n;
        }
        return null;
    }

    public static JsonNode getNode(PropertyParts pp, JsonNode node, int depth, boolean createIfMissing) {
        JsonNode root = node;
        String parts[] = pp.getParts();
        boolean isNumber[] = pp.getNumber();
        if (node == null) {
            return MissingNode.getInstance();
        }
        for (int i = 0; i < depth; i++) {
            if (isNumber[i] == true) {
                int ind = Integer.parseInt(parts[i]);
                if (!node.isArray()) {
                    // not array
                    return null;
                }
                JsonNode tNode = node.get(ind);
                if (tNode == null && createIfMissing) {
                    tNode = JsonNodeFactory.instance.arrayNode();
                    addToParent(root, depth, node, pp, tNode);
                }
                node = tNode;
            } else {
                JsonNode tNode = node.get(parts[i]);
                if (tNode == null && createIfMissing) {
                    tNode = JsonNodeFactory.instance.objectNode();
                    addToParent(root, depth, node, pp, tNode);
                }
                node = tNode;
            }
            if (node == null) {
                return null;
            }

        }
        return node;
    }

    private static void addToParent(JsonNode root, int depth, JsonNode prev, PropertyParts pp, JsonNode curr) {
        if (depth == 0) {
            ((ObjectNode) root).put(pp.getParts()[0], curr);
        }
        boolean isNumber = pp.getNumber()[depth - 1];
        if (isNumber) {
            // implied extra layer here!  Not sure this is right.  It maybe we need to check for non array node cases
            String arrayName = pp.getParts()[depth - 2];
            ArrayNode an = null;
            try {
                if (prev.isArray()) {
                    an = (ArrayNode) prev;
                } else {
                    an = (ArrayNode) prev.get(arrayName);
                }
            } catch (ClassCastException cce) {
                Console.println();
            }
            if (an == null) {
                an = JsonNodeFactory.instance.arrayNode();
                ((ObjectNode) prev).put(arrayName, an);
            }
            int i = Integer.parseInt(pp.getParts()[depth - 1]);
            if (!an.has(i)) {
                an.insert(0, curr);
            } else {
                an.set(i, curr);
            }
        } else {
            ((ObjectNode) prev).put(pp.getParts()[depth - 1], curr);
        }
    }

    public static final String resolveJsonVariable(String value) {
        if (value == null) {
            return null;
        }
        return resolveJsonVariable(value, true, null, JVSProperties.getProperties().getJsonNode());
    }

    public static final String resolveJsonVariable(String value, boolean recurse, JsonNode topArgs, JsonNode args) {
        StringBuilder builder = new StringBuilder();
        StringProperty prop = new StringProperty("", "", "");
        if (value == null) {
            Log.util.error("Not given a value to resolve");
            return null;
        }
        int index = value.indexOf(VariableStart);
        int lastPos = 0;
        int endIndex = 0;
        while (index != -1) {
            builder.append(value, lastPos, index);
            endIndex = value.indexOf(VariableEnd, index + 1);
            if (endIndex == -1) {
                // error case, we dont have a matching }
                // is break the correct thing?
                break;
            } else {
                String variable = value.substring(index + 2, endIndex).toLowerCase();

                String substValue = getString(topArgs, args, variable, prop);


                if (recurse) {
                    if (StringUtil.nullOrEmptyString(substValue)) {

                    } else {
                        if (substValue.indexOf(VariableStart) != -1) {
                            // this variable contained a variable
                            substValue = resolveJsonVariable(substValue, true, topArgs, args);
                            //args.put(variable, substValue);
                            // Should we replace or leave in raw form?
                        }
                        builder.append(substValue);
                    }
                } else {
                    builder.append(substValue);
                }
                lastPos = endIndex + 1;
                index = value.indexOf(VariableStart, lastPos + 1);
            }
        }
        // append tail
        builder.append(value.substring(lastPos));
        return builder.toString();
    }


    /**
     * Props2String - get the output of Properties as a String.
     *
     * @return String or null on error.
     */
    public static String Props2String(Properties props) {
        byte b[] = Props2Bytes(props);
        return (b == null) ? null : new String(b);
    }
}
