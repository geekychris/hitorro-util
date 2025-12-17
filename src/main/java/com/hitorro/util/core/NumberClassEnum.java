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


import java.util.HashMap;

/**
 * Created because there is no convenient way to ask what type is a number
 */
public enum NumberClassEnum {
    IntegerE(Integer.class) {
        public Number parseFromString(String s) {
            return Integer.parseInt(s);
        }
    },
    LongE(Long.class) {
        public Number parseFromString(String s) {
            return Long.parseLong(s);
        }
    },
    ShortE(Short.class) {
        public Number parseFromString(String s) {
            return Short.parseShort(s);
        }
    },
    DoubleE(Double.class) {
        public Number parseFromString(String s) {
            return Double.parseDouble(s);
        }
    },
    FloatE(Float.class) {
        public Number parseFromString(String s) {
            return Float.parseFloat(s);
        }
    };

    private static HashMap<Class, NumberClassEnum> map;
    private Class c;

    NumberClassEnum(Class c) {
        this.c = c;
        setEntry(c, this);
    }

    public static NumberClassEnum get(Class c) {
        return map.get(c);
    }

    public static NumberClassEnum get(Number n) {
        return map.get(n.getClass());
    }

    private static void setEntry(Class c, NumberClassEnum nce) {
        if (map == null) {
            map = new HashMap<Class, NumberClassEnum>();
        }
        map.put(c, nce);
    }

    public abstract Number parseFromString(String s);
}
