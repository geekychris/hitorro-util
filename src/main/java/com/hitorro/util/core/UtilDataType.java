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
package com.hitorro.util.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.BaseMappers;
import com.hitorro.util.core.string.StringUtil;

import java.util.HashMap;
import java.util.Set;

/**
 * Copy of the DataType enum found in features.  Shame you cant subclass enums :-}
 */
public enum UtilDataType {
    byteType(0, "byte", 1, Byte.class, BaseMappers.S2B) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Byte.MIN_VALUE;
            }
            return Byte.parseByte(s);
        }
    },
    shortType(1, "short", 2, Short.class, BaseMappers.S2S) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Short.MIN_VALUE;
            }
            return Short.parseShort(s);
        }
    },
    longType(2, "long", 8, Long.class, BaseMappers.S2L) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Long.MIN_VALUE;
            }
            return Long.parseLong(s);
        }
    },
    integerType(3, "int", 4, Integer.class, BaseMappers.S2I) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Integer.MIN_VALUE;
            }
            return Integer.parseInt(s);
        }
    },
    floatType(4, "float", 4, Float.class, BaseMappers.S2F) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Float.MIN_VALUE;
            }
            return Float.parseFloat(s);
        }
    },
    doubleType(5, "double", 8, Double.class, BaseMappers.S2D) {
        public Object getNullValue(String s) {
            if (StringUtil.nullOrEmptyString(s)) {
                return Double.MIN_VALUE;
            }
            return Double.parseDouble(s);
        }
    },
    stringType(6, "string", -1, String.class, BaseMappers.S2String) {
        public Object getNullValue(String s) {
            return s;
        }
    };

    private static HashMap<String, UtilDataType> s_byShortName;
    private static TIntObjectHashMap<UtilDataType> byOrd;
    private String name;
    private int width;
    private Class classType;
    private int ord;
    private BaseMapper fromString;

    UtilDataType(int ord, String name, int width, Class classType, BaseMapper fromString) {
        this.ord = ord;
        this.name = name;
        this.width = width;
        this.classType = classType;
        this.fromString = fromString;
        setMapEntry(this);
    }

    public static Set<String> getValidValues() {
        return s_byShortName.keySet();
    }

    public static int size() {
        return s_byShortName.size();
    }

    public static UtilDataType getByName(String name) {
        return s_byShortName.get(name.toLowerCase());
    }

    public static UtilDataType getByStableOrdinal(int ordinal) {
        return byOrd.get(ordinal);
    }

    private static void setMapEntry(UtilDataType filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, UtilDataType>();
        }
        if (byOrd == null) {
            byOrd = new TIntObjectHashMap();
        }
        s_byShortName.put(filter.getName(), filter);
        byOrd.put(filter.getStableOrdinal(), filter);
    }

    public abstract Object getNullValue(String s);

    public int getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }

    public Class getClassType() {
        return classType;
    }

    /**
     * BaseMapper that converts from a string representation to a boxed typed value.
     *
     * @return
     */
    public BaseMapper getFromStringMapper() {
        return fromString;
    }

    public int getStableOrdinal() {
        return ord;
    }
}

