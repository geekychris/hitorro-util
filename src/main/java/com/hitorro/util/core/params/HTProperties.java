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
package com.hitorro.util.core.params;

import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.filefilters.FilenameExtensionFilter;
import com.hitorro.util.io.filefilters.OrCollection;
import com.hitorro.util.json.keys.PropertyException;
import com.hitorro.util.xml.SAXUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public final class HTProperties implements Map<String, String> {
    public static final String VariableEnd = "}";
    public static final String VariableStart = "${";
    public static final String XmlPropertyKey = "xmlprops";
    // singleton default properties
    private static HTProperties _defaultProperties = null;
    private long configHash = -1;
    private TreeMap<String, String> properties;

    public HTProperties() {
        properties = null;
    }

    public static HTProperties getProperties() {
        return _defaultProperties;
    }

    public static void load(InputStream inStream, Map<String, String> loadMap) throws IOException {
        load(inStream, loadMap, false);
    }

    public static void load(InputStream inStream, Map<String, String> loadMap, boolean lowerCaseKey) throws IOException {
        char[] convtBuf = new char[1024];
        LineReader lr = new LineReader(inStream);

        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            if (lowerCaseKey) {
                key = key.toLowerCase();
            }
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            loadMap.put(key, value);
        }
    }

    private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    /**
     * Read properties from a specified location.
     *
     * @param fileLocation
     * @return the HTProperties object, which will be empty (but usable) if the properties weren't found
     */
    public static HTProperties read(File fileLocation) {
        HTProperties result = new HTProperties();
        result.readFile(fileLocation, false);
        return result;
    }

    public static String resolveVariable(String value, boolean recurse, Map<String, String> map) {
        return resolveVariable(value, recurse, null, map);
    }

    /**
     * Resolve a string that contains a HiTorro variable
     *
     * @param value
     * @param recurse
     * @return
     */
    public static final String resolveVariable(String value, boolean recurse, Map<String, String> topArgs, Map<String, String> args) {
        StringBuilder builder = new StringBuilder();
        if (value == null) {
            Log.util.error("Not given a value to resolve");
            return null;
        }
        int index = value.indexOf(VariableStart);
        int lastPos = 0;
        int endIndex = 0;
        while (index != -1) {
            builder.append(value, lastPos, index);
            endIndex = value.indexOf(VariableEnd, index + 1);
            if (endIndex == -1) {
                // error case, we dont have a matching }
                // is break the correct thing?
                break;
            } else {
                String variable = value.substring(index + 2, endIndex).toLowerCase();

                String substValue;
                if (topArgs != null) {
                    substValue = topArgs.get(variable);
                    if (substValue == null) {
                        substValue = args.get(variable);
                    }
                } else {
                    substValue = args.get(variable);
                }


                if (recurse) {
                    if (StringUtil.nullOrEmptyString(substValue)) {

                    } else {
                        if (substValue.indexOf(VariableStart) != -1) {
                            // this variable contained a variable
                            substValue = resolveVariable(substValue, true, topArgs, args);
                            args.put(variable, substValue);
                        }
                        builder.append(substValue);
                    }
                } else {
                    builder.append(substValue);
                }
                lastPos = endIndex + 1;
                index = value.indexOf(VariableStart, lastPos + 1);
            }
        }
        // append tail
        builder.append(value.substring(lastPos));
        return builder.toString();
    }

    public static void resolveVariables(Map<String, String> map) {
        if (map == null) {
            // nothing to do
            return;
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // look for the magic variable boundary
            if (value != null) {
                int openVar = value.indexOf(VariableStart);
                if (openVar >= 0) {
                    map.put(key, resolveVariable(value, true, map));
                }
            }
        }

        return;
    }

    public synchronized static void setDefaultProperties(HTProperties props, boolean runDiff) {

        _defaultProperties = props;
    }

    public long getConfigHash() {
        if (configHash == -1) {
            configHash = getConfigHashAux(getMap());
        }
        return configHash;
    }

    public int size() {
        return properties.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }

    public String put(final String key, final String value) {
        return setProperty(key, value);
    }

    public String put(final String key, final int value) {
        return setProperty(key, Integer.toString(value));
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */

    public String put(final String key, final long value) {
        return setProperty(key, Long.toString(value));
    }

    public String remove(final Object key) {
        return properties.remove(key.toString().toLowerCase());
    }

    public void putAll(final Map<? extends String, ? extends String> m) {
        properties.putAll(m);
    }

    public void clear() {
        properties.clear();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Collection<String> values() {
        return properties.values();
    }

    public Set<Entry<String, String>> entrySet() {
        return properties.entrySet();
    }

    public void dumpProperties() {
        Console.println(Env.getPrettyPrintedProperties(this.properties));
    }

    /**
     * Dump the properties to a debug response
     *
     * @param response
     */
    public void dumpPropertiesToResponse(Response response, ResponseShape header) {
        response.setResponseShape(header);

        Set<Entry<String, String>> set = properties.entrySet();
        Iterator<Entry<String, String>> iter = set.iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            response.addRow(entry.getKey(), entry.getValue());
        }
        response.end();
    }

    public List<String> getChildKeys(String prefix) {
        return MapUtil.getChildKeys(prefix, properties);
    }

    public TreeMap<String, String> getMap() {
        return properties;
    }

    /**
     * Get a property value.
     *
     * @param key Key of the value to get
     * @return the value of the key, or null if the key wasn't found
     */

    public String get(final Object key) {
        if (properties != null) {
            String val = properties.get(key.toString().toLowerCase());

            return val;
        }
        return null;
    }

    public TreeMap<String, String> getSubMap(String prefix) {
        return MapUtil.getSubMap(properties, prefix);
    }

    public TreeMap<String, String> getSubMap(String root, String key) {
        return getSubMap(Fmt.S("%s.%s", root, key));
    }

    public List<KeyMap> getSubMaps(String root) {
        List<KeyMap> list = new ArrayList();
        List<String> childKeys = getChildKeys(root);
        for (String key : childKeys) {
            TreeMap<String, String> map = getSubMap(root, key);
            if (map != null) {
                list.add(new KeyMap(key, map));
            }
        }
        return list;
    }

    public Properties getSubProperties(String prefix) {
        return MapUtil.getPropertySubProperties(properties, prefix);
    }

    /**
     * Read all the properties files from a given directory. This allows you to place multiple properties files in a
     * known location, for easier management of modules. We will read *.properties.  Each file will be merged into this
     * object's properties.
     *
     * @param dirFile The path to the directory to be read.
     */
    public void readDirectory(File dirFile, List<File> filesConsidered) {
        // create a new HTProperties for this directory layer
        // no collisions are allowed in properties within the layer
        HTProperties dirLayer = new HTProperties();
        FilenameFilter filter = new FilenameExtensionFilter("properties", true);
        FilenameFilter xmlfilter = new FilenameExtensionFilter(XmlPropertyKey, true);
        OrCollection or = new OrCollection(filter, xmlfilter);
        List<File> propertyFiles = FileUtil.findFilteredFiles(or, dirFile, false);

        for (File pfile : propertyFiles) {
            dirLayer.readFile(pfile, false);
            filesConsidered.add(pfile);
        }

        // now apply the new layer into the old properties
        // we allow (and expect) collisions here - this layer takes precedence
        readMap(dirLayer.properties, true, dirFile);

        return;
    }

    public boolean readFileFromXML(File fileLocation, Map<String, String> map) {

        ReadPropsFromXMLHandler handler = new ReadPropsFromXMLHandler();
        handler.map = map;
        try {
            SAXUtil.readSax(fileLocation, handler);
            return true;
        } catch (IOException e) {
            Log.util.error("Unable to read property file %s %s %e", fileLocation, e, e);
        } catch (ParserConfigurationException e) {
            Log.util.error("Unable to read property file %s %s %e", fileLocation, e, e);
        } catch (SAXException e) {
            Log.util.error("Unable to read property file %s %s %e", fileLocation, e, e);
        }
        return false;

    }

    public boolean readFileFromXML(InputStream in, Map<String, String> map) {

        ReadPropsFromXMLHandler handler = new ReadPropsFromXMLHandler();
        handler.map = map;
        try {
            SAXUtil.readSax(in, handler);
            return true;
        } catch (IOException e) {
            Log.util.error("Unable to read property %s %e", e, e);
        } catch (ParserConfigurationException e) {
            Log.util.error("Unable to read property %s %s %e", e, e);
        } catch (SAXException e) {
            Log.util.error("Unable to read property %s %s %e", e, e);
        }
        return false;

    }

    /**
     * Read properties from a file into this object.
     *
     * @param fileLocation The location of the parameters file we are reading
     */
    public void readFile(File fileLocation, boolean allowOveride) {
        InputStream in = null;
        try {
            // read properties from the file
            TreeMap<String, String> newProps = new TreeMap<String, String>();
            String ext = FileUtil.getFileExtension(fileLocation);
            if (ext.equals(XmlPropertyKey)) {
                if (!readFileFromXML(fileLocation, newProps)) {
                    Log.util.fatal("Unable to load property file %s", fileLocation);
                }
            } else {
                in = FileUtil.getBufferedFileInputStream(fileLocation);
                load(in, newProps);
                in.close();
            }

            // apply in properties
            if (properties == null) {
                // don't have to apply, just use these new properties
                properties = newProps;
            } else {
                // need to apply in the properties
                readMap(newProps, allowOveride, fileLocation);
            }
        } catch (FileNotFoundException fnfe) {
            String msg = "Could not find properties file at: " + fileLocation;
            throw new PropertyException(msg);
        } catch (IOException ioe) {
            String msg = "Error reading properties file " + fileLocation + " is " + ioe;
            throw new PropertyException(msg);
        }

    }

    public void readFile(InputStream in, boolean allowOveride, boolean isXML) {
        try {
            // read properties from the file
            TreeMap<String, String> newProps = new TreeMap<String, String>();
            if (isXML) {
                if (!readFileFromXML(in, newProps)) {
                    Log.util.fatal("Unable to load property from stream");
                }
            } else {
                load(in, newProps);
                in.close();
            }

            // apply in properties
            if (properties == null) {
                // don't have to apply, just use these new properties
                properties = newProps;
            } else {
                // need to apply in the properties
                readMap(newProps, allowOveride, null);
            }
        } catch (FileNotFoundException fnfe) {
            String msg = "Could not find properties file";
            throw new PropertyException(msg);
        } catch (IOException ioe) {
            String msg = "Error reading properties file ioe" + ioe.getMessage();
            throw new PropertyException(msg);
        }

    }

    public void readMap(Map<String, String> props, boolean overrideAllowed, File containingFile) {
        if (props == null) {
            // this is allowed, but we have nothing to do
            return;
        }

        if (properties == null) {
            properties = new TreeMap<String, String>();
        }
        Set<String> keys = props.keySet();
        Iterator<String> keyI = keys.iterator();
        while (keyI.hasNext()) {
            String key = keyI.next();
            Object old = properties.put(key, props.get(key));
            if (!overrideAllowed && old != null) {
                String file = "Internal";
                if (containingFile != null) {
                    try {
                        file = containingFile.getCanonicalPath();
                    } catch (IOException e) {
                        Log.util.error("%s %e", e, e);
                    }
                }
                throw new PropertyException(Fmt.S("Collision on property %s reading file %s ", key, file));
            }
        }

        return;
    }

    /**
     * Read a apply of values into this properties. The values in the apply (key, value) will be merged into our current
     * properties.
     *
     * @param props Properties to be added
     */
    public void readMap(Map<String, String> props, File containingFile) {
        readMap(props, false, containingFile);
    }

    /**
     * Resolve a string that contains a HiTorro variable
     *
     * @param value
     * @param recurse
     * @return
     */
    public String resolveVariable(String value, boolean recurse) {
        return resolveVariable(value, recurse, properties);
    }

    public String resolveVariableOveride(String value, boolean recurse, Map<String, String> map) {
        return resolveVariable(value, recurse, map, this.properties);
    }

    public void resolveVariables() {
        resolveVariables(properties);
    }

    /**
     * Set the value for a property.
     *
     * @param key   Key of the value to set
     * @param value value for the key, may be null
     * @return the previous value of the property
     */
    public String setProperty(String key, String value) {
        if (properties == null) {
            // lazy creation of wrapped properties object
            properties = new TreeMap<String, String>();
        }
        return properties.put(key, value);
    }

    /**
     * Write out the properties file to the location where it came from.
     */
    public void write(String outPath) {
        // tell the properties file where it is going
        if (properties == null) {
            // create an empty properties
            properties = new TreeMap<String, String>();
        }
        try {
            PrintWriter out = new PrintWriter(outPath);
            out.println("# HiTorro properties");
            out.println();

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                out.print(key);
                out.print('=');
                out.println(value);
            }
            out.close();
        } catch (IOException ioe) {
            System.err.println("Couldn't write properties to " + outPath);
        }
    }

    public void write(OutputStream os) {
        // tell the properties file where it is going
        if (properties == null) {
            // create an empty properties
            properties = new TreeMap<String, String>();
        }

        PrintWriter out = new PrintWriter(os);
        out.println("# HiTorro properties");
        out.println();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            out.print(key);
            out.print('=');
            out.println(value);
        }
        out.close();
    }

    private long getConfigHashAux(TreeMap<String, String> map) {
        long fp = 0;

        for (String key : map.keySet()) {
            String val = map.get(key);
            fp = FPHash64.combineFingerPrints(fp, FPHash64.getFP(val));
        }
        return fp;
    }

    static class LineReader {
        byte[] inBuf = new byte[8192];

        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        char[] lineBuf = new char[1024];

        public LineReader(InputStream inStream) {
            this.inStream = inStream;
        }

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = inStream.read(inBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        return len;
                    }
                }
                //The line below is equivalent to calling a
                //ISO8859-1 decoder.
                c = (char) (0xff & inBuf[inOff++]);
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = inStream.read(inBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        //skip the leading whitespace characters in following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }
}
