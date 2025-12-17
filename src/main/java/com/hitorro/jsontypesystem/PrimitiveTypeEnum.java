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
package com.hitorro.jsontypesystem;

import com.hitorro.util.core.EnumContext;

import java.util.Date;

public enum PrimitiveTypeEnum {
    String("string", String.class, -1, false, null) {
        public Object convertFromString(String s) {
            return s;
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Date("date", java.util.Date.class, -1, false, null) {
        public Object convertFromString(String s) {
            return new Date(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Long("long", Long.class, 64, false, null) {
        public Object convertFromString(String s) {
            return java.lang.Long.parseLong(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Int("int", Integer.class, 32, false, PrimitiveTypeEnum.Long) {
        public Object convertFromString(String s) {
            return Integer.parseInt(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Double("Double", Double.class, 64, true, null) {
        public Object convertFromString(String s) {
            return java.lang.Double.parseDouble(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Float("float", Float.class, 32, true, PrimitiveTypeEnum.Double) {
        public Object convertFromString(String s) {
            return java.lang.Float.parseFloat(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },

    Boolean("Boolean", Boolean.class, 1, false, null) {
        public Object convertFromString(String s) {
            return java.lang.Boolean.getBoolean(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }

    },
    Short("short", Short.class, 16, false, PrimitiveTypeEnum.Int) {
        public Object convertFromString(String s) {
            return java.lang.Short.parseShort(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },

    Byte("byte", Byte.class, 8, false, Short) {
        public Object convertFromString(String s) {
            return java.lang.Byte.parseByte(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    };

    public static EnumContext<PrimitiveTypeEnum> primitiveContext;
    private String m_name;
    private Class implClass;
    private int width;
    private boolean isFloat;
    private PrimitiveTypeEnum superior;

    PrimitiveTypeEnum(String name, Class implClass, int width, boolean isFloat, PrimitiveTypeEnum superior) {
        m_name = name.toLowerCase();
        this.implClass = implClass;
        this.width = width;
        this.isFloat = isFloat;
        this.superior = superior;

        setMapEntry(this, name);
    }

    public static PrimitiveTypeEnum getFromClass(Class c) {
        for (PrimitiveTypeEnum e : PrimitiveTypeEnum.values()) {
            if (e.superior == e) {
                return e;
            }
        }
        return null;
    }

    private static void setMapEntry(PrimitiveTypeEnum filter, String shortName) {
        if (primitiveContext == null) {
            primitiveContext = new EnumContext("primitivetype");
        }
        primitiveContext.setNames(filter, shortName);
    }

    public Class getImplClass() {
        return implClass;
    }

    public int getWidth() {
        return width;
    }

    public boolean isFloat() {
        return isFloat;
    }

    public PrimitiveTypeEnum getSuperior() {
        return superior;
    }

    public abstract Object convertFromString(String s);

    public abstract String convertToString(Object o);
}
