package ht.util.core;


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
