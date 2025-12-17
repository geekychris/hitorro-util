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
package com.hitorro.util.basefile.tools.queue.reader;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

public class GoodFileKeyWriter {
    private File parentFile;
    private File keyFile;
    private String goodKey;

    public GoodFileKeyWriter() {

    }

    public GoodFileKeyWriter(File keyFile) {
        this.keyFile = keyFile;
        parentFile = keyFile.getParentFile();
        if (keyFile.exists()) {
            StringBuilder sb = FileUtil.readFromFile(keyFile);
            if (sb != null) {
                goodKey = sb.toString();
            }
        }
    }

    public String getSource() {
        return keyFile.getAbsolutePath();
    }

    public String[] getParts() {
        if (StringUtil.nullOrEmptyString(goodKey)) {
            return new String[0];
        }
        return StringUtil.tokenizeFromSingleChar(goodKey, "/");
    }

    public String getGoodKey() {
        return goodKey;
    }


    public Date getLastGoodKeyDate() {
        return null;
    }

    public void reset() {
        goodKey = null;
        if (FileUtil.notNullAndExists(keyFile)) {
            keyFile.delete();
        }
    }

    public void setName(File file) {
        keyFile = file;
        if (keyFile.exists()) {
            StringBuilder sb = FileUtil.readFromFile(keyFile);
            if (sb != null) {
                goodKey = sb.toString();
            }
        }
    }

    public void save(String key) {
        goodKey = key;
        try {
            FileUtil.ensureDirectoryExists(parentFile);
            FileUtil.writeStringToFile(keyFile, key);
        } catch (FileNotFoundException e) {
            Log.util.error("GoodFileKeyWriter unable to write last good key %s %e", e, e);
        }
    }
}
