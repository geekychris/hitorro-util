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
import com.hitorro.util.io.FileChangeSet;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class JVSSingleFilePropertyReader implements JVSPropertiesReader {
    private boolean participatesInFileDiffCheck;
    private FileChangeSet changeContainer;
    private File file;

    public JVSSingleFilePropertyReader(boolean participatesInFileDiffCheck) {
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

    public void getProperties(JVS props, JVS cmdLineArgs) throws Exception {
        file = this.getFile(props);
        if (participatesInFileDiffCheck) {
            List<File> filesConsidered = new ArrayList();
            filesConsidered.add(file);
            changeContainer = new FileChangeSet(filesConsidered);
        }
        if (FileUtil.nullOrNotExist(file)) {
            return;
        }
        JVS tmp = JVS.read(file);
        props.merge(tmp);
    }

    public abstract File getFile(JVS propsSoFar);
}

