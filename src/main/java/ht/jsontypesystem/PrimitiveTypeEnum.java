package ht.jsontypesystem;

import ht.util.core.EnumContext;

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
