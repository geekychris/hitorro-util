package ht.util.core;

import java.util.HashMap;

/**
 *
 */
public enum LogicalOperEnum {
    Xor("^") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 ^ d2;
        }

    },
    Or("||") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 || d2;
        }
    },
    And("&&") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 && d2;
        }
    };


    private static HashMap<String, LogicalOperEnum> s_byShortName;
    private String m_name;

    LogicalOperEnum(String name) {
        m_name = name.toLowerCase();
        setMapEntry(this);
    }

    public static LogicalOperEnum get(String name) {
        return s_byShortName.get(name.toLowerCase());
    }

    public static int size() {
        return s_byShortName.size();
    }

    private static void setMapEntry(LogicalOperEnum filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, LogicalOperEnum>();
        }
        s_byShortName.put(filter.getName(), filter);
    }

    public abstract boolean oper(boolean d1, boolean d2);

    public String getName() {
        return m_name;
    }
}
