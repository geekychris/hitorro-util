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
package com.hitorro.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.core.params.JsonKeyMap;

import java.text.ParseException;
import java.util.*;

public class JSONUtil {

    public static boolean nullOrEmpty(JsonNode node) {
        return node == null || node.isNull() || ((node.isArray() || node.isObject()) && node.size() == 0);
    }

    public static final ArrayNode toJsonStringArray(List list) {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        for (Object r : list) {
            arr.add(r.toString());
        }
        return arr;
    }

    public static final String mergeValues(JsonNode map, String mergeToken) {
        ObjectNode on = (ObjectNode) map;
        StringBuilder builder = new StringBuilder();
        String keys[] = new String[on.size()];
        JsonNode values[] = new JsonNode[on.size()];
        JSONUtil.populateKeyValue(on, keys, values);
        for (JsonNode n : values) {
            String s = n.textValue();
            if (builder.length() > 0) {
                builder.append(mergeToken);
            }
            builder.append(s);
        }
        return builder.toString();
    }

    public static List<JsonKeyMap> getSubMaps(JsonNode node, String root) {
        List<JsonKeyMap> list = new ArrayList();
        JsonNode childKeys = node.get(root);
        if (childKeys == null) {
            return list;
        }
        Iterator<Map.Entry<String, JsonNode>> iter = childKeys.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> e = iter.next();
            list.add(new JsonKeyMap(e.getKey(), e.getValue()));
        }
        return list;
    }

    public static final JsonNode addCallstack(Throwable t) {
        StackTraceElement[] elements = t.getStackTrace();
        ArrayNode node = JsonNodeFactory.instance.arrayNode();

        for (StackTraceElement e : elements) {
            node.add(e.toString());
        }
        return node;
    }

    public static ObjectNode map2json(Map<String, String> args) {
        ObjectNode on = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            on.put(entry.getKey(), entry.getValue());
        }
        return on;
    }

    /**
     * Adds to the string buffer the call stack of the target exception and that of all its chained exceptions.
     *
     * @param t the target exception.
     */
    public static final JsonNode addCallstackChain(Throwable t) {
        ObjectNode on = null;
        ObjectNode root = null;
        Throwable cause;
        String time = DateResolution.json.getNowFormatted();
        for (cause = t.getCause(); cause != null; cause = cause.getCause()) {
            if (cause != null) {
                ObjectNode tmp = JsonNodeFactory.instance.objectNode();
                tmp.set("cause", addCallstack(cause));
                tmp.put("message", cause.getMessage());
                tmp.put("exception", t.getClass().getCanonicalName());

                if (on == null) {
                    tmp.put("time", time);
                    root = tmp;
                } else {
                    on.set("caused_by", tmp);

                }
                on = tmp;
            }
        }
        return root;
    }

    /**
     * Convert a Java value to a Jackson JsonNode.
     */
    public static JsonNode ensureJsonNode(Object value) {
        return switch (value) {
            case null -> null;
            case JsonNode jn -> jn;
            case String s -> JsonNodeFactory.instance.textNode(s);
            case Integer i -> JsonNodeFactory.instance.numberNode(i);
            case Long l -> JsonNodeFactory.instance.numberNode(l);
            case Float f -> JsonNodeFactory.instance.numberNode(f);
            case Double d -> JsonNodeFactory.instance.numberNode(d);
            case Boolean b -> JsonNodeFactory.instance.booleanNode(b);
            case Short s -> JsonNodeFactory.instance.numberNode(s.intValue());
            case Byte b -> JsonNodeFactory.instance.numberNode(b.intValue());
            default -> null;
        };
    }


    public static String getString(JsonNode n) {
        if (n == null) {
            return null;
        }
        return n.textValue();
    }

    public static Date getDate(JsonNode n) {
        if (n == null) {
            return null;
        }

        try {
            Date d = new Date();
            String s = DateResolution.json.getFormatted(d);
            return DateResolution.json.parse(n.textValue());
        } catch (ParseException e) {
            return null;
        }
    }

    public static List<String> getStringList(JsonNode n) {
        if (n == null || !n.isArray()) {
            return null;
        }
        ArrayList<String> l = new ArrayList();
        for (JsonNode elem : n) {
            l.add(elem.textValue());
        }
        return l;
    }

    public static long[] getLongArray(JsonNode n) {
        if (n == null || !n.isArray()) {
            return null;
        }
        long[] ret = new long[n.size()];
        for (int i = 0; i < n.size(); i++) {
            ret[i] = n.get(i).longValue();
        }
        return ret;
    }

    public static double getDouble(JsonNode n, double defaultVal) {
        if (n == null) {
            return defaultVal;
        }
        return n.doubleValue();
    }

    public static long getLong(JsonNode n, long defaultVal) {
        if (n == null) {
            return defaultVal;
        }
        return n.longValue();
    }


    /**
     * Transfer the Key and values of the ObjectMap to two arrays.
     *
     * @param node
     * @param key
     * @param val
     */
    public static void populateKeyValue(ObjectNode node, String key[], JsonNode val[]) {
        Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
        int i = 0;
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> e = iter.next();

            if (val != null) {
                val[i] = e.getValue();
            }
            key[i++] = e.getKey();
        }
    }

}
