package ht.util.core;

import ht.util.core.string.Fmt;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Simple wrapper around the console, by default uses stdout with carriage return.
 *
 * @author ccollins
 */
public class Console {
    public static PrintStream m_out = System.out;
    public static PrintStream m_outErr = System.err;

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
        m_outErr.println();
    }

    /**
     * Print provided text and a new line to stdout
     *
     * @param text to print out
     */
    public static final void eprintln(String text) {
        m_outErr.println(text);
    }

    /**
     * Print provided text and a new line to stdout formatting arguments (must use %s)
     *
     * @param text to print out formatted with all the arguments
     */
    public static final void eprintln(String text, Object... args) {
        m_outErr.println(Fmt.S(text, args));
    }

    /**
     * Print text to stdout
     */
    public static final void eprint(String text) {
        m_outErr.print(text);
    }

    /**
     * Print text to stdout formatting provided args using %s syntax
     *
     * @param text to format
     * @param args to insert into formatted text
     */
    public static final void eprint(String text, Object... args) {
        m_outErr.print(Fmt.S(text, args));
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
