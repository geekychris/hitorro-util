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
package com.hitorro.jsontypesystem;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.IntegerUtil;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.filefilters.FilenameExtensionFilter;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JVSUtils {
    public static final BooleanProperty DebugMode = new BooleanProperty("debug", "If debug mode is enabled loads extra config files", false);

    /**
     * convert old style configs to JSON
     *
     * @param configs
     * @throws IOException
     */
    public static void convertConfigsDir(final BaseFile configs) throws IOException {
        BaseFile files[] = configs.listFiles();
        for (BaseFile bf : files) {
            String ext = bf.getExtension();
            if ("properties".equals(ext) || "xmlprops".equals(ext)) {
                HTProperties p = new HTProperties();
                p.readFile(bf.getLocalFileIfPossible(), true);
                JVS jvs = JVSUtils.convertMapToJVS(p);

                BaseFile pair = bf.getPeerExtension("json");
                // jvs.write(pair);
            }
        }
    }

    public static JVS convertMapToJVS(HTProperties props) {
        JVS jvs = new JVS();
        return convertMapToJVS(props.getMap(), jvs);
    }

    public static String convertPropPathToJVS(String s) {
        String parts[] = StringUtil.tokenizeFromSingleChar(s, ".");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (sb.length() > 0) {
                sb.append(".");
            }
            sb.append(parts[i]);
            if (!IntegerUtil.isNumber(parts[i])) {
                if (i + 1 < parts.length) {
                    if (IntegerUtil.isNumber(parts[i + 1])) {
                        sb.append("[");
                        sb.append(parts[i + 1]);
                        sb.append("]");
                        i++;
                    }
                }
            }
        }
        return sb.toString();
    }

    public static JVS convertMapToJVS(Map<String, String> t) {
        return convertMapToJVS(t, new JVS());
    }

    public static JVS convertMapToJVS(Map<String, String> t, JVS jvs) {
        for (Map.Entry<String, String> e : t.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            k = convertPropPathToJVS(k);
            try {
                Console.println("%s %s", k, v);
                jvs.set(k, v);

            } catch (PropaccessError propaccessError) {
                propaccessError.printStackTrace();
            }

        }
        return jvs;
    }

    public static List<File> getProps(final JVS jvs, final JVS props, final File directory) throws Exception {
        List<File> filesConsidered = new ArrayList();
        boolean debugMode = DebugMode.apply(jvs);
        String serverType = Env.ServerType.apply(jvs);
        readDirectory(props, directory, filesConsidered);
        if (!StringUtil.nullOrEmptyString(serverType)) {
            File subDirFile = new File(directory, serverType);
            if (subDirFile.exists()) {
                readDirectory(props, subDirFile, filesConsidered);
            }
        }
        if (debugMode) {
            File debugDir = new File(directory, "debug");
            if (debugDir.exists()) {
                readDirectory(props, debugDir, filesConsidered);
            }
        }
        return filesConsidered;
    }

    public static void readFile(File file, JVS props) throws Exception {
        JVS fileJvs = JVS.read(file);
        props.merge(fileJvs);
    }

    public static void readDirectory(JVS uberJVS, File dirFile, List<File> filesConsidered) throws Exception {
        // create a new HTProperties for this directory layer
        // no collisions are allowed in properties within the layer
        JVS dirJvs = new JVS();
        FilenameFilter filter = new FilenameExtensionFilter("json", true);
        List<File> propertyFiles = FileUtil.findFilteredFiles(filter, dirFile, false);

        for (File pfile : propertyFiles) {
            JVS fileJvs = JVS.read(pfile);
            dirJvs.merge(fileJvs);
            filesConsidered.add(pfile);
        }

        uberJVS.merge(dirJvs);
    }


    enum S {
        C, D, N
    }


}
