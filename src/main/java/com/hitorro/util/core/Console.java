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

import com.hitorro.util.core.string.Fmt;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Simple wrapper around the console, by default uses stdout with carriage return.
 *
 * @author ccollins
 */
public class Console {
    public static PrintStream m_out = System.out;
    public static PrintStream outErr = System.err;

    /**
     * Print a new line to stdout with carriage return
     */
    public static final void println() {
        m_out.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void println(String text) {
        m_out.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void println(String text, Object... args) {
        m_out.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void print(String text) {
        m_out.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void print(String text, Object... args) {
        m_out.print(Fmt.S(text, args));
    }

    /**
     * Print a new line to stdout with carriage return
     */
    public static final void println(PrintStream out) {
        out.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void println(PrintStream out, String text) {
        out.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void println(PrintStream out, String text,
                                     Object... args) {
        out.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void print(PrintStream out, String text) {
        out.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void print(PrintStream out, String text,
                                   Object... args) {
        out.print(Fmt.S(text, args));
    }

    /**
     * Print a new line to stdout with carriage return
     */
    public static final void eprintln() {
        outErr.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void eprintln(String text) {
        outErr.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void eprintln(String text, Object... args) {
        outErr.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void eprint(String text) {
        outErr.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void eprint(String text, Object... args) {
        outErr.print(Fmt.S(text, args));
    }

    /**
     * Print a new line to stdout with carriage return
     */
    public static final void eprintln(PrintStream out) {
        out.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void eprintln(PrintStream out, String text) {
        out.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void eprintln(PrintStream out, String text,
                                      Object... args) {
        out.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void eprint(PrintStream out, String text) {
        out.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void eprint(PrintStream out, String text,
                                    Object... args) {
        out.print(Fmt.S(text, args));
    }

    /**
     * Print text to buffer
     *
     * @param text to format to insert into formatted text
     */
    public static final void bprint(StringBuilder buff, String text) {
        buff.append(text);
    }

    /**
     * Print text to buffer formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void bprint(StringBuilder buff, String text,
                                    Object... args) {
        Fmt.f(buff, text, args);
    }


    public static final void appendIfNotEmpty(StringBuilder buff, String text) {
        if (buff.length() > 0) {
            buff.append(text);
        }
    }

    public static final void appendIfNotEmpty(StringBuilder buff, char c) {
        if (buff.length() > 0) {
            buff.append(c);
        }
    }

    /**
     * Print provided text and a new line to buffer
     *
     * @param text to buffer
     */
    public static final void bprintln(StringBuilder buff, String text) {
        buff.append(text);
        buff.append(Constants.NewLineChar);
    }

    /**
     * Print blank like to buffer
     * <p/>
     * to print out blank line to buffer
     */
    public static final void bprintln(StringBuilder buff) {
        buff.append(Constants.NewLineChar);
    }

    /**
     * Print provided text and a new line to buffer formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void bprintln(StringBuilder buff, String text,
                                      Object... args) {
        Fmt.f(buff, text, args);
        buff.append(Constants.NewLineChar);
    }

    /**
     * Print a new line to stdout with carriage return
     */
    public static final void println(PrintWriter out) {
        out.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void println(PrintWriter out, String text) {
        out.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void println(PrintWriter out, String text,
                                     Object... args) {
        if (out == null) {
            return;
        }
        out.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void print(PrintWriter out, String text) {
        out.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void print(PrintWriter out, String text,
                                   Object... args) {
        out.print(Fmt.S(text, args));
    }
}
