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

import com.hitorro.util.core.Env;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.io.FileChangeSet;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Load properties from the provided directory (if it exists). If it exists we also will look to see if there is a sub
 * directory for the server type and load everything in there too. If we are in debug mode a final "debug" directory
 * will be looked for, so in most part we have:
 * <p/>
 * directory/*.properties /<servertype>/*.properties /debug/*.properties
 */
public class DirectoryReadingPropertiesReader implements PropertiesReader {
    protected File otherDir = null;
    private DirectoryType type;
    private boolean participatesInFileDiffCheck;
    private FileChangeSet changeContainer;

    public DirectoryReadingPropertiesReader(DirectoryType type, boolean participatesInFileDiffCheck) {
        this.type = type;
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;

    }

    public DirectoryReadingPropertiesReader(File other, boolean participatesInFileDiffCheck) {
        this.type = DirectoryType.Other;
        otherDir = other;
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;

    }

    /**
     * Detect if files have changed
     *
     * @return
     */
    public boolean havePropertiesChanged() {
        if (!participatesInFileDiffCheck) {
            // never considers any changes
            return false;
        }
        if (changeContainer == null) {
            return false;
        }
        return changeContainer.hasAnythingChangedResetting();
    }

    public File getDirectory() {

        switch (type) {
            case Home:
                return new File(Env.getHome(), "config");
            case Bin:
                return new File(Env.getBin(), "config");
            case Other:
                return otherDir;
        }
        return null;
    }

    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        File directory = getDirectory();

        List<File> filesConsidered = PropReaderUtil.getProps(cmdLineArgs, props, directory);
        if (participatesInFileDiffCheck) {
            changeContainer = new FileChangeSet(filesConsidered);
        }
    }


}
