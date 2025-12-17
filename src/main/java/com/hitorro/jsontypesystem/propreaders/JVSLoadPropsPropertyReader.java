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
package com.hitorro.jsontypesystem.propreaders;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JVSUtils;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileChangeSet;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Load property files defined on the commandline.  A file on the command line could be a single file or a directory
 * containing further property files.  In this case the same scanning model applies as @link
 * JVSDirectoryReadingPropertiesReader
 */
public class JVSLoadPropsPropertyReader implements JVSPropertiesReader {
    public static final String LoadProps = "loadprops";
    private FileChangeSet changeContainer;

    private boolean participatesInFileDiffCheck;

    public JVSLoadPropsPropertyReader(boolean participatesInFileDiffCheck) {
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;
    }

    public void getProperties(JVS props, JVS cmdLineArgs) throws Exception {
        String loadProp = cmdLineArgs.getString(LoadProps);
        String loadProps[] = StringUtil.tokenizeFromSingleChar(loadProp, ",");
        List<File> files = new ArrayList();
        for (String prop : loadProps) {
            if (!StringUtil.nullOrEmptyOrBlankString(prop)) {
                File loadPropsFile = new File(prop);
                if (FileUtil.notNullAndExists(loadPropsFile)) {
                    if (loadPropsFile.isDirectory()) {
                        files.addAll(JVSUtils.getProps(cmdLineArgs, props, loadPropsFile));
                    } else {
                        files.add(loadPropsFile);
                        JVSUtils.readFile(loadPropsFile, props);
                    }
                } else {
                    Console.eprintln("Property file %s does not exist, cannot initialize", prop);
                    System.exit(-1);
                }
            }
        }
        changeContainer = new FileChangeSet(files);

    }

    public boolean havePropertiesChanged() {
        if (!participatesInFileDiffCheck) {
            return false;
        }
        if (changeContainer == null) {
            return false;
        }
        return changeContainer.hasAnythingChangedResetting();
    }
}
