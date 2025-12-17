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
