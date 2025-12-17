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
 *
 */
public enum Operator {
    Mult("*") {
        @Override
        public double oper(final double d1, final double d2) {
            return d1 * d2;
        }

        @Override
        public float oper(final float d1, final float d2) {
            return d1 * d2;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 * d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 * d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 * d2);
        }
    },
    Minus("-") {
        @Override
        public double oper(final double d1, final double d2) {
            return d1 - d2;
        }

        @Override
        public float oper(final float d1, final float d2) {
            return d1 - d2;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 - d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 - d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 - d2);
        }
    },
    Divide("/") {
        @Override
        public double oper(final double d1, final double d2) {
            return d1 / d2;
        }

        @Override
        public float oper(final float d1, final float d2) {
            return d1 / d2;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 / d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 / d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 / d2);
        }
    },
    Add("+") {
        @Override
        public double oper(final double d1, final double d2) {
            return d1 + d2;
        }

        @Override
        public float oper(final float d1, final float d2) {
            return d1 + d2;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 + d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 + d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 + d2);
        }
    },
    BitwiseAnd("&") {
        @Override
        public double oper(final double d1, final double d2) {
            // NA
            return 0;
        }

        @Override
        public float oper(final float d1, final float d2) {
            // NA
            return 0;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 & d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 & d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 & d2);
        }
    },
    BitwiseOr("|") {
        @Override
        public double oper(final double d1, final double d2) {
            // NA
            return 0;
        }

        @Override
        public float oper(final float d1, final float d2) {
            // NA
            return 0;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 | d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 | d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 | d2);
        }
    },
    BitwiseXOr("^") {
        @Override
        public double oper(final double d1, final double d2) {
            // NA
            return 0;
        }

        @Override
        public float oper(final float d1, final float d2) {
            // NA
            return 0;
        }

        @Override
        public long oper(final long d1, final long d2) {
            return d1 ^ d2;
        }

        @Override
        public int oper(final int d1, final int d2) {
            return d1 ^ d2;
        }

        @Override
        public short oper(final short d1, final short d2) {
            return (short) (d1 ^ d2);
        }
    };


    private static HashMap<String, Operator> s_byShortName;
    private String m_name;

    Operator(String name) {
        m_name = name.toLowerCase();
        setMapEntry(this);
    }

    public static Operator get(String name) {
        return s_byShortName.get(name.toLowerCase());
    }

    public static int size() {
        return s_byShortName.size();
    }

    private static void setMapEntry(Operator filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, Operator>();
        }
        s_byShortName.put(filter.getName(), filter);
    }

    public abstract double oper(double d1, double d2);

    public abstract float oper(float d1, float d2);

    public abstract long oper(long d1, long d2);

    public abstract int oper(int d1, int d2);

    public abstract short oper(short d1, short d2);

    public String getName() {
        return m_name;
    }
}
