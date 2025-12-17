package ht.util.core;

import java.util.Date;
import java.util.HashMap;

/**
 *
 */
public enum CompOperEnum {
    Equals("=") {
        public boolean isTrue(double d1, double d2) {
            return d1 == d2;
        }

        public boolean isTrue(float d1, float d2) {
            return d1 == d2;
        }

        public boolean isTrue(int d1, int d2) {
            return d1 == d2;
        }

        public boolean isTrue(long d1, long d2) {
            return d1 == d2;
        }

        public boolean isTrue(short d1, short d2) {
            return d1 == d2;
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return d1.equals(d2);
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() == d2.getTime();
        }
    },
    GreaterThan(">") {
        public boolean isTrue(double d1, double d2) {
            return d1 > d2;
        }

        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 > d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 > d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 > d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 > d2;
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return d1.compareTo(d2) > 0;
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() > d2.getTime();
        }
    },
    LessThan("<") {
        public boolean isTrue(double d1, double d2) {
            return d1 < d2;
        }

        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 < d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 < d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 < d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 < d2;
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return d1.compareTo(d2) < 0;
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() < d2.getTime();
        }
    },
    GreaterThanOrEqual(">=") {
        @Override
        public boolean isTrue(final String d1, final String d2) {
            return d1.compareTo(d2) >= 0;
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() >= d2.getTime();
        }

        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 >= d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 >= d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 >= d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 >= d2;
        }

        public boolean isTrue(double d1, double d2) {
            return d1 >= d2;
        }
    },
    LessThanOrEqual("<=") {
        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() <= d2.getTime();
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return d1.compareTo(d2) <= 0;
        }

        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 <= d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 <= d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 <= d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 <= d2;
        }

        public boolean isTrue(double d1, double d2) {
            return d1 <= d2;
        }
    },
    NotEqual("!=") {
        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return !d1.equals(d2);
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() != d2.getTime();
        }

        public boolean isTrue(double d1, double d2) {
            return d1 != d2;
        }
    },
    NotEqualGTST("<>") {
        @Override
        public boolean isTrue(final float d1, final float d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final long d1, final long d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final int d1, final int d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final short d1, final short d2) {
            return d1 != d2;
        }

        @Override
        public boolean isTrue(final String d1, final String d2) {
            return !d1.equals(d2);
        }

        @Override
        public boolean isTrue(final Date d1, final Date d2) {
            return d1.getTime() != d2.getTime();
        }

        public boolean isTrue(double d1, double d2) {
            return d1 != d2;
        }
    };


    public static EnumContext<CompOperEnum> compContext;
    private static HashMap<String, CompOperEnum> s_byShortName;
    private String m_name;

    CompOperEnum(String name) {
        m_name = name.toLowerCase();
        setMapEntry(this);
    }

    private static void setMapEntry(CompOperEnum filter) {
        if (compContext == null) {
            compContext = new EnumContext<CompOperEnum>("compenum");
        }
        compContext.setNames(filter, filter.getName());
    }

    public abstract boolean isTrue(double d1, double d2);

    public abstract boolean isTrue(float d1, float d2);

    public abstract boolean isTrue(long d1, long d2);

    public abstract boolean isTrue(int d1, int d2);

    public abstract boolean isTrue(short d1, short d2);

    public abstract boolean isTrue(String d1, String d2);

    public abstract boolean isTrue(Date d1, Date d2);

    public String getName() {
        return m_name;
    }
}
