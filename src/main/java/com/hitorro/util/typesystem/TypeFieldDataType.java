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
package com.hitorro.util.typesystem;

import com.hitorro.util.core.EnumContext;
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;


/**
 *
 */
public enum TypeFieldDataType {

    Long("long", Long.class, 64, false, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return java.lang.Long.valueOf(hois.readLong());
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeLong((Long) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Long.parseLong(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Int("int", Integer.class, 32, false, Long) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return hois.readInt();
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeInt((Integer) o);
        }

        public Object convertFromString(String s) {
            return Integer.parseInt(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Short("short", Short.class, 16, false, TypeFieldDataType.Int) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return java.lang.Short.valueOf(hois.readShort());
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeShort((Short) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Short.parseShort(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },

    Byte("byte", Byte.class, 8, false, TypeFieldDataType.Int) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return java.lang.Byte.valueOf(hois.readByte());
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeByte((Byte) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Byte.parseByte(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },

    Double("double", Double.class, 64, true, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return java.lang.Double.valueOf(hois.readDouble());
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeDouble((Double) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Double.parseDouble(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },
    Float("float", Float.class, 32, true, Double) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return java.lang.Float.valueOf(hois.readFloat());
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeFloat((Float) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Float.parseFloat(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }
    },


    String("string", String.class, -1, false, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException, ClassNotFoundException {
            return hois.readString();
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeString((String) o);
        }

        public Object convertFromString(String s) {
            return s;
        }

        public String convertToString(Object o) {
            return o.toString();
        }

    },
    Date("date", Date.class, -1, false, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException, ClassNotFoundException {
            return hois.readDate();
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeDate((Date) o);
        }

        public Object convertFromString(String s) {
            return new Date(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }

    },
    Boolean("boolean", Boolean.class, 1, false, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException {
            return hois.readBoolean();
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException {
            hoos.writeBoolean((Boolean) o);
        }

        public Object convertFromString(String s) {
            return java.lang.Boolean.getBoolean(s);
        }

        public String convertToString(Object o) {
            return o.toString();
        }

    },

    HTSerializable("basetype", BaseType.class, -1, false, null) {
        public Object readObject(HTObjectInputStream hois) throws IOException, ClassNotFoundException, StoreException {
            return hois.readVersionedObject();
        }

        public void writeObject(HTObjectOutputStream hoos, Object o) throws IOException, StoreException {
            hoos.writeVersionedObject((BaseType) o);
        }

        // DONT KNOW how to
        public Object convertFromString(String s) {
            return null;
        }

        public String convertToString(Object o) {
            return o.toString();
        }

    };

    public static EnumContext<TypeFieldDataType> fieldDataType = new EnumContext("fieldtype");
    private static HashMap<Class, TypeFieldDataType> s_byClass;
    private static HashMap<String, TypeFieldDataType> s_byShortName;
    private Class clazz;
    private String shortName;
    private int width;
    private boolean isFloat;
    private TypeFieldDataType superior;


    TypeFieldDataType(String shortName, Class implClass, int width, boolean isFloat, TypeFieldDataType superior) {
        clazz = implClass;
        this.shortName = shortName;
        this.width = width;
        this.isFloat = isFloat;
        if (superior == null) {
            this.superior = this;
        } else {
            this.superior = superior;
        }
        setMapEntry(this);

    }

    public static TypeFieldDataType getFromClass(Class c) {
        TypeFieldDataType t = s_byClass.get(c);
        if (t != null) {
            return t;
        }
        t = s_byShortName.get(c.getName());
        if (t != null) {
            return t;
        }
        // blindly return htserializable
        return HTSerializable;
    }

    public static TypeFieldDataType getFromShortName(String c) {
        TypeFieldDataType t = s_byShortName.get(c);
        if (t != null) {
            return t;
        }
        // blindly return htserializable
        return String;
    }

    private static void setMapEntry(TypeFieldDataType filter) {
        if (fieldDataType == null) {
            fieldDataType = new EnumContext("filters");
        }
        fieldDataType.setNames(filter, filter.shortName);
        if (s_byClass == null) {
            s_byClass = new HashMap<Class, TypeFieldDataType>();
        }
        s_byClass.put(filter.getSerializedClass(), filter);

        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, TypeFieldDataType>();
        }
        s_byShortName.put(filter.getShortName(), filter);
    }

    public abstract Object readObject(HTObjectInputStream hois) throws IOException, ClassNotFoundException, StoreException;

    public abstract void writeObject(HTObjectOutputStream hoos, Object o) throws IOException, StoreException;

    public abstract Object convertFromString(String s);

    public abstract String convertToString(Object o);

    public Class getSerializedClass() {
        return clazz;
    }

    public TypeFieldDataType getSuperior() {
        return superior;
    }

    public boolean getIsFloat() {
        return isFloat;
    }

    public int getWidth() {
        return width;
    }

    public String getShortName() {
        return shortName;
    }

}
