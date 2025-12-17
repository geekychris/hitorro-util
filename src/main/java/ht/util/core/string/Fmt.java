package ht.util.core.string;

import ht.jsontypesystem.JVS;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.CommandArgs;
import ht.util.core.classes.ClassUtil;
import ht.util.core.params.HTProperties;

import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

/**
 * Ultra simple string formatter that deals with object that you can format using toString(). Further knows how to print
 * callstacks if you use the %e option
 *
 * @author ccollins
 */
public final class Fmt {
    private static final int CompleteNoArgProcessing = 2;

    private static final int CompleteProcessArg = 1;

    private static final int NotComplete = 0;


    public static final void addCallstack(Throwable t, StringBuilder buff, boolean xml) {
        StackTraceElement[] elements = t.getStackTrace();
        buff.append(t.getClass().getSimpleName());
        String msg = t.getMessage();
        if (msg != null) {
            buff.append(": ").append(msg);
        }
        if (xml) {
            buff.append("<br> Stack:<br>     ");
            StringBuilderUtil.mergeWithJoinToken(buff, elements, "<br>");
        } else {
            buff.append("\n Stack:\n     ");
            StringBuilderUtil.mergeWithJoinToken(buff, elements, "\n     ");
        }
    }


    /**
     * Adds to the string buffer the call stack of the target exception and that of all its chained exceptions.
     *
     * @param t    the target exception.
     * @param buff string buffer to receive the stack traces.
     */
    public static final void addCallstackChain(Throwable t, StringBuilder buff, boolean xml) {
        Throwable cause = t.getCause();
        if (cause != null) {
            // first iterate and find out the depth of the stack
            int causes = 1;
            while (cause != null) {
                cause = cause.getCause();
                causes++;
            }
            if (xml) {
                buff.append("<br>").append(causes).append(
                        " additional stack traces expected<br>");
            } else {
                buff.append('\n').append(causes).append(
                        " additional stack traces expected\n");
            }
        }

        addCallstack(t, buff, xml);
        for (cause = t.getCause(); cause != null; cause = cause.getCause()) {
            if (xml) {
                buff.append(" caused by:<br>");
            } else {
                buff.append(" caused by:\n");
            }

            addCallstack(cause, buff, xml);
        }
    }

    public static final boolean f(StringBuilder buff, final String pattern,
                                  final Object... args) {
        int patternLength = pattern.length();
        int currP = 0;
        int currArg = 0;
        Object arg = null;
        while (currP < patternLength) {
            char currC = pattern.charAt(currP++);
            if (currC != '%') {
                buff.append(currC);
            } else {
                int complete = NotComplete;
                while (currP < patternLength && complete == NotComplete) {
                    currC = pattern.charAt(currP++);

                    /*
                     * Its a special character...for now we support: %s - print
                     * string %e - print stack trace %% - print a % char
                     *
                     * later we will want to address I18N by having a syntax
                     * perhaps like {0} - arg position
                     */
                    switch (currC) {
                        case 'd':
                        case 'e':
                        case 'm':
                        case 's':
                        case 'u':
                        case 'r':
                            complete = CompleteProcessArg;
                            break;
                        case '%':
                            complete = CompleteNoArgProcessing;
                            buff.append("%");
                            break;
                        default:
                            // Unknown state character
                            buff.append("<<UNKNOWN STATE CHARACTER>>");
                            complete = CompleteNoArgProcessing;
                            break;
                    } // end while
                }
                if (complete == CompleteProcessArg) {
                    // pull an argument and stuff it in the buffer
                    if (currArg > args.length) {
                        arg = "<<Insufficient arguments provided>>";
                    } else {
                        arg = args[currArg++];
                    }

                    if (arg == null) {
                        buff.append("<<NULL>>");
                    } else {
                        if (currC == 's') {
                            // we will implant formatting code here later on.
                            buff.append(arg);
                        } else if (currC == 'e') {
                            // We think this is a callstack, lets get the string
                            if (arg instanceof Throwable) {
                                addCallstackChain((Throwable) arg, buff, false);
                            } else {
                                // Doesnt look like a throwable
                                buff.append(">>");
                                buff.append(arg);
                                buff.append("<<");
                            }
                        } else if (currC == 'x') {
                            // We think this is a callstack, lets get the string
                            if (arg instanceof Throwable) {
                                addCallstackChain((Throwable) arg, buff, true);
                            } else {
                                // Doesnt look like a throwable
                                buff.append(">>");
                                buff.append(arg);
                                buff.append("<<");
                            }
                        } else if (currC == 'm') {
                            // We think this is a callstack, lets get the string
                            if (arg instanceof Map) {
                                CommandArgs.getArgObject(buff, (Map) arg);
                            } else {
                                // Doesnt look like a throwable
                                buff.append(">>");
                                buff.append(arg);
                                buff.append("<<");
                            }
                        } else if (currC == 'd') {
                            // we will implant formatting code here later on.
                            buff.append(arg);
                            char c = buff.charAt(buff.length() - 1);
                            if (c != '/') {
                                buff.append('/');
                            }
                        } else if (currC == 'u') {
                            // want to print the source path to the current class.
                            URL u = ClassUtil.getFileForObject(arg);
                            buff.append(u.toExternalForm());
                        } else if (currC == 'r') {
                            // url encode
                            buff.append(StringUtil.encodeUrl(arg.toString()));
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Format an array of doubles using unit format above
     *
     * @param da
     */
    public static final String formatArray(double[] da) {
        StringBuilder buf = new StringBuilder();
        if (da == null) {
            buf.append("<<NULL>>");
        } else {
            for (double d : da) {
                buf.append(formatUnits(d)).append(", ");
            }
        }
        return buf.toString();
    }

    /**
     * Format an array of longs using unit format above
     *
     * @param la
     */
    public static final String formatArray(long[] la) {
        StringBuilder buf = new StringBuilder();
        if (la == null) {
            buf.append("<<NULL>>");
        } else {
            for (long l : la) {
                buf.append(formatUnits(l)).append(", ");
            }
        }
        return buf.toString();
    }

    /**
     * Format an array of Strings using unit format above
     *
     * @param sa
     */
    public static final String formatArray(String[] sa) {
        StringBuilder buf = new StringBuilder();
        if (sa == null) {
            buf.append("<<NULL>>");
        } else {
            for (String s : sa) {
                buf.append(s).append(", ");
            }
        }
        return buf.toString();
    }

    /**
     * Format a time as a string.
     *
     * @param time The difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     *             This is what you get from System.currentTimeMillis().
     * @return a formatted string
     */
    public static final String formatDateTime(long time) {
        return DateFormat
                .getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(
                        new Date(time));
    }

    public static final String formatDateTimeDelta(long start, long stop) {
        long deltaSeconds = (start - stop) / 1000;
        long seconds = deltaSeconds % 60;
        long deltaMinutes = deltaSeconds / 60;
        long minutes = deltaMinutes % 60;
        long deltaHours = deltaMinutes / 60;
        long hours = deltaHours % 24;
        long days = deltaHours / 24;
        return Fmt.S("D/h/m/s %s:%s:%s:%s", days, hours, minutes, seconds);
    }

    /**
     * Format the time assuming double is ms.us
     *
     * @param t
     * @return Formatted string representing the time in the appropriately scoped units
     */
    public static final String formatTime(double t) {
        String units, format;

        if (t >= 1000) {
            units = "s";
            t = t / 1000;
        } else if (t >= 1) {
            units = "ms";
        } else if (t >= 1e-3) {
            units = "us";
            t = t * 1e3;
        } else {
            units = "ns";
            t = t * 1e6;
        }

        if (t >= 100) {
            format = "000";
        } else if (t >= 10) {
            format = "00.0";
        } else {
            format = "0.00";
        }

        return new DecimalFormat(format + units).format(t);
    }

    /**
     * Formats "u", a value of "units", with 3 significant digits in scientific notation, using the exponents <none>,
     * e3, e6, or e9.
     */
    public static String formatUnits(double u) {
        String exp;
        String format;

        if (u >= 1e12) {
            exp = "e12";
            u = u / 1e12;
        } else if (u >= 1e9) {
            exp = "e9";
            u = u / 1e9;
        } else if (u >= 1e6) {
            exp = "e6";
            u = u / 1e6;
        } else if (u >= 1e3) {
            exp = "e3";
            u = u / 1e3;
        } else {
            exp = "";
        }

        if (u >= 100) {
            format = "000";
        } else if (u >= 10) {
            format = "00.0";
        } else {
            format = "0.00";
        }

        return new DecimalFormat(format + exp).format(u);
    }

    /**
     * Do positional based string substitution using the process global properties
     *
     * @param pattern
     * @return
     */
    public static final String P(final String pattern) {
        return JVSProperties.getProperties().resolveJsonVariable(pattern);
    }

    /**
     * Do positional based string substitution passing a listFiles of variables and their values
     *
     * @param pattern
     * @param args
     * @return
     */
    public static final String P(final String pattern, Map<String, String> args) {
        return HTProperties.resolveVariable(pattern, false, null, args);
    }

    /**
     * Do positional based string substitution passing a listFiles of variables and their values
     *
     * @param pattern
     * @return
     */
    public static final String PO(final String pattern, JVS top) {
        return JVSProperties.getProperties().resolveJsonVariable(pattern, top);
    }

    /**
     * take a format string and n args. Produce a string that is the format string with the substituted arguments in
     * place. Supports two types of argument substition:
     * <p/>
     * %s - argument has the toString method called to get a string representation %e - checked to see if a throwable,
     * if so the stack is printed.
     * <p/>
     * Example:
     * <p/>
     * Fmt.S("HTHello %s", "Chris"); = "HTHello Chris"
     *
     * @param pattern
     * @param args
     * @return formatted string
     */
    public static final String S(final String pattern, final Object... args) {
        StringBuilder buffer = new StringBuilder();
        f(buffer, pattern, args);
        return buffer.toString();
    }

    public static final String Sargs(final String pattern, final Object args[]) {
        StringBuilder buffer = new StringBuilder();
        f(buffer, pattern, args);
        return buffer.toString();
    }
}
