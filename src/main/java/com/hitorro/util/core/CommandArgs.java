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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;

/**
 * Utilities associated with parsing out key value pairs of the form:
 * <p/>
 * key=value key1= value ....
 *
 * @author chris
 */
public class CommandArgs {

    /**
     * Convert a hashtable of variables back into command line args
     * <p/>
     * NOTE: that the value is only quoted, we dont attempt to deal with strings that contain quotes already.
     *
     * @param targetMap
     * @return
     */
    public static final String getArgString(Map<String, String> targetMap) {
        StringBuilder b = new StringBuilder();
        getArgString(b, targetMap);
        return b.toString();
    }

    public static final void getArgString(StringBuilder b, Map<String, String> targetMap) {
        Set<Map.Entry<String, String>> es = targetMap.entrySet();
        for (Map.Entry<String, String> entry : es) {
            b.append(entry.getKey());
            b.append("=\"");
            b.append(entry.getValue());
            b.append("\" ");
        }
    }

    public static final void getArgObject(StringBuilder b, Map<String, Object> targetMap) {
        Set<Map.Entry<String, Object>> es = targetMap.entrySet();
        for (Map.Entry<String, Object> entry : es) {
            b.append(entry.getKey());
            b.append("=\"");
            b.append(entry.getValue().toString());
            b.append("\" ");
        }
    }

    /**
     * Given a string that consists of key value pairs, scan the string and extract using the following rules:
     * <p/>
     * key is alpha numeric, white space is not allowed. = is used as assignment operator, it can be surrounded by
     * spaces optionally value is alpha numeric, if it contains spaces, it must be enclosed in single or double quotes.
     *
     * @param param
     * @param supportFlags allows flags (keys with no value) to be specified
     */
    public static final JVS getParameters(String param, boolean supportFlags)
            throws ParseException, PropaccessError {
        return getParameters(param, supportFlags, false);
    }

    /**
     * Given a string that consists of key value pairs, scan the string and extract using the following rules:
     * <p/>
     * key is alpha numeric, white space is not allowed. = is used as assignment operator, it can be surrounded by
     * spaces optionally value is alpha numeric, if it contains spaces, it must be enclosed in single or double quotes.
     *
     * @param param
     * @param supportFlags allows flags (keys with no value) to be specified
     */


    public static final JVS getParameters(String param, boolean supportFlags, boolean lowerCaseKey)
            throws ParseException, PropaccessError {
        JVS jvs = new JVS();
        return getParameters(param, supportFlags, lowerCaseKey, jvs);
    }

    public static final JVS getParameters(String param, boolean supportFlags, boolean lowerCaseKey, JVS props)
            throws ParseException, PropaccessError {

        StringBuilder buff = new StringBuilder();
        boolean inQuotes = false;
        States state = States.WhiteSpace;
        int size = param.length();
        char c = ' ';
        String key = null;
        for (int i = 0; i < size; i++) {
            c = param.charAt(i);
            switch (state) {
                case WhiteSpace:
                    if (!Character.isWhitespace(c)) {
                        state = States.KEY;
                        buff.append(c);
                    }
                    break;
                case KEY:
                    if (Character.isWhitespace(c)) {
                        state = States.WhiteSpaceBeforeEquals;
                    } else if (c == '=') {
                        key = buff.toString();
                        buff.setLength(0);
                        state = States.WhiteSpaceFollowingEquals;
                    } else {
                        buff.append(c);
                    }
                    break;
                case WhiteSpaceBeforeEquals:
                    if (!Character.isWhitespace(c) && c == '=') {
                        key = buff.toString();
                        buff.setLength(0);
                        state = States.WhiteSpaceFollowingEquals;
                    } else {
                        if (supportFlags) {
                            state = States.KEY;
                            if (lowerCaseKey) {
                                props.set(buff.toString().toLowerCase(), "true");
                            } else {
                                props.set(buff.toString(), "true");
                            }
                            buff.setLength(0);
                            buff.append(c);
                        } else {
                            throw new ParseException("Looking for space or = sign", i);
                        }
                    }
                    break;
                case WhiteSpaceFollowingEquals:
                    if (c != ' ') {
                        state = States.Value;
                        if (c == '\"') {
                            inQuotes = true;
                        } else {
                            buff.append(c);
                        }
                    }
                    break;
                case Value:
                    if (c == '\"') {
                        inQuotes = !inQuotes;
                    } else {
                        if (inQuotes) {
                            buff.append(c);
                        } else {
                            if (Character.isWhitespace(c)) {
                                state = States.WhiteSpace;
                                if (lowerCaseKey) {
                                    props.set(key.toLowerCase(), buff.toString());
                                } else {
                                    props.set(key, buff.toString());
                                }
                                buff.setLength(0);
                            } else {
                                buff.append(c);
                            }
                        }
                    }
            }

        }
        // End
        if (state == States.KEY) {
            if (supportFlags) {
                state = States.WhiteSpaceBeforeEquals;
                if (lowerCaseKey) {
                    props.set(buff.toString().toLowerCase(), "true");
                } else {
                    props.set(buff.toString(), "true");
                }
                buff.setLength(0);
                buff.append(c);
            } else {
                throw new ParseException(Fmt.S("Found key with no matching value at end of buffer %s",
                        buff.toString()), 0);
            }
        }
        if (state == States.Value) {
            if (inQuotes == false) {
                if (lowerCaseKey) {
                    props.set(key.toLowerCase(), buff.toString());
                } else {
                    props.set(key, buff.toString());
                }
                buff.setLength(0);
            } else {
                throw new ParseException(Fmt.S("properties terminated while within quote: %s", param), param.length());
            }
        }

        return props;
    }

    /**
     * Parse a string that is of the format: key=value key1=value...etc
     * <p/>
     * if supportFlags = true then a key with no value such:
     * <p/>
     * key key1=value.... is equal to:
     * <p/>
     * key=true key1=value...
     *
     * @param args
     * @param supportFlags
     */
    public static void parseArgs(String args,
                                 boolean supportFlags,
                                 boolean lowerCaseKey,
                                 JVS targetMap)
            throws ParseException, PropaccessError {
        JVS map = getParameters(args, supportFlags);
        targetMap.merge(map);
    }


    enum States {
        WhiteSpace, KEY, WhiteSpaceBeforeEquals, WhiteSpaceFollowingEquals, Value
    }
}
