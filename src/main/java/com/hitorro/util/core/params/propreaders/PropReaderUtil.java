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
package com.hitorro.util.core.params.propreaders;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JVSUtils;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 Utils for loading props from files
 */
public class PropReaderUtil {
    public static final BooleanProperty DebugMode = new BooleanProperty("debug", "If debug mode is enabled loads extra config files", false);

    public static List<File> getProps(final Map<String, String> cmdLineArgs, final HTProperties props, final File directory) {
        JVS cmd = JVSUtils.convertMapToJVS(cmdLineArgs, new JVS());
        List<File> filesConsidered = new ArrayList();
        boolean debugMode = DebugMode.apply(cmd);
        String serverType = Env.ServerType.apply(cmd);
        props.readDirectory(directory, filesConsidered);
        if (!StringUtil.nullOrEmptyString(serverType)) {
            File subDirFile = new File(directory, serverType);
            if (subDirFile.exists()) {
                props.readDirectory(subDirFile, filesConsidered);
            }
        }
        if (debugMode) {
            File debugDir = new File(directory, "debug");
            if (debugDir.exists()) {
                props.readDirectory(debugDir, filesConsidered);
            }
        }
        return filesConsidered;
    }
}
