package ht.util.core;

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
