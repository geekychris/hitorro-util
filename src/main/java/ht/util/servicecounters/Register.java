package ht.util.servicecounters;

import ht.util.core.JMXUtil;
import ht.util.core.string.Fmt;

/**
 * Holds state about counts
 */
public abstract class Register implements Comparable<Register>, RegisterMBean {
    protected CounterSet cs;
    private String name;
    private String description;

    public Register(CounterSet cs, String name, String description) {
        this.cs = cs;
        this.name = name;
        this.description = description;
        JMXUtil.registerForJMX(this, "HiTorro", Fmt.S("counter.%s", cs.getName()), name);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return Fmt.S("%s.%s", cs.getName(), name);
    }

    public abstract void clock(int depth);

    public abstract double getAsDouble(boolean prior, int i);

    public abstract long getAsLong(boolean prior, int i);

    public abstract String getAsString(boolean prior, int i);

    public abstract double getAsDouble();

    public abstract long getAsLong();

    public abstract String getValue();

    public boolean isCascading() {
        return true;
    }

    public int compareTo(Register r) {
        return getFullName().compareTo(r.getFullName());
    }
}
