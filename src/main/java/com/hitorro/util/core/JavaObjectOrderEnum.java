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

import com.hitorro.util.core.string.StringUtil;

import java.util.HashMap;

/**
 *
 */
public enum JavaObjectOrderEnum {
    STRING_ASC(String.class, true, "AS") {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    STRING_DESC(String.class, false, "DS") {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    },

    INTEGER_ASC(String.class, true, "AI") {
        public int compare(Object o1, Object o2) {
            Integer s1 = (Integer) o1;
            Integer s2 = (Integer) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    INTEGER_DESC(String.class, false, "DI") {
        public int compare(Object o1, Object o2) {
            Integer s1 = (Integer) o1;
            Integer s2 = (Integer) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    },

    LONG_ASC(String.class, true, "AL") {
        public int compare(Object o1, Object o2) {
            Long s1 = (Long) o1;
            Long s2 = (Long) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    LONG_DESC(String.class, false, "DL") {
        public int compare(Object o1, Object o2) {
            Long s1 = (Long) o1;
            Long s2 = (Long) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    },


    FLOAT_ASC(String.class, true, "AF") {
        public int compare(Object o1, Object o2) {
            Float s1 = (Float) o1;
            Float s2 = (Float) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    FLOAT_DESC(String.class, false, "DF") {
        public int compare(Object o1, Object o2) {
            Float s1 = (Float) o1;
            Float s2 = (Float) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    },

    DOUBLE_ASC(String.class, true, "AD") {
        public int compare(Object o1, Object o2) {
            Double s1 = (Double) o1;
            Double s2 = (Double) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    DOUBLE_DESC(String.class, false, "DD") {
        public int compare(Object o1, Object o2) {
            Double s1 = (Double) o1;
            Double s2 = (Double) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    },

    SHORT_ASC(String.class, true, "AX") {
        public int compare(Object o1, Object o2) {
            Short s1 = (Short) o1;
            Short s2 = (Short) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    },
    SHORT_DESC(String.class, false, "DX") {
        public int compare(Object o1, Object o2) {
            Short s1 = (Short) o1;
            Short s2 = (Short) o2;
            int c = s1.compareTo(s2);
            if (c == 0) {
                return 0;
            }
            if (c > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    private static HashMap<Class, JavaObjectOrderEnum> s_byShortClassAsc;
    private static HashMap<Class, JavaObjectOrderEnum> s_byShortClassDescending;
    private static HashMap<String, JavaObjectOrderEnum> s_byShortName;
    private Class m_class;
    private String name;
    private boolean ascending;

    JavaObjectOrderEnum(Class clazz, boolean ascending, String name) {
        this.m_class = clazz;
        this.ascending = ascending;
        this.name = name;
        setMapEntry(this, ascending, name);
    }

    public static JavaObjectOrderEnum getFilterByClass(Class c, boolean ascending) {
        if (ascending) {
            return s_byShortClassAsc.get(c);
        } else {
            return s_byShortClassDescending.get(c);
        }
    }

    public static JavaObjectOrderEnum getByName(String name) {
        if (name.length() > 2) {
            name = name.substring(0, 2);
        }
        return s_byShortName.get(name);
    }

    public static int size() {
        return s_byShortClassAsc.size() + s_byShortClassDescending.size();
    }

    private static void setMapEntry(JavaObjectOrderEnum filter, boolean ascending, String name) {
        if (ascending) {
            if (s_byShortClassAsc == null) {
                s_byShortClassAsc = new HashMap<Class, JavaObjectOrderEnum>();
            }
        } else {
            if (s_byShortClassDescending == null) {
                s_byShortClassDescending = new HashMap<Class, JavaObjectOrderEnum>();
            }
        }
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, JavaObjectOrderEnum>();
        }

    }

    /**
     * Generate the field names from the barrel set.
     *
     * @param barrel
     * @return
     */
    public static String[] getNames(JavaObjectOrderEnum barrel[]) {
        String arr[] = new String[barrel.length];
        for (int i = 0; i < barrel.length; i++) {
            arr[i] = StringUtil.strcat("%s_%s", barrel[i].getName(), "_", i);
        }
        return arr;
    }

    /**
     * Actual comparator functionality for composite comparisons.
     *
     * @param left
     * @param right
     * @param barrel
     * @param names
     * @return
     */
    public static final int compare(JavaObjectOrderEnumCompInterface left,
                                    JavaObjectOrderEnumCompInterface right,
                                    JavaObjectOrderEnum barrel[],
                                    String names[]) {
        int l = barrel.length;
        Object leftSF[] = left.getSortFrame(barrel, names, l);
        Object rightSF[] = right.getSortFrame(barrel, names, l);
        for (int i = 0; i < l; i++) {
            int v = barrel[i].compare(leftSF[i], rightSF[i]);
            if (v > 0) {
                return 1;
            }
            if (v < 0) {
                return -1;
            }
        }
        return 0;
    }

    public abstract int compare(Object o1, Object o2);

    public Class getSortClass() {
        return m_class;
    }

    public String getName() {
        return name;
    }

    public boolean isAscending() {
        return ascending;
    }
}

