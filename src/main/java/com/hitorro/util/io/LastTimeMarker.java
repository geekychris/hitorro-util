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
package com.hitorro.util.io;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * <p/>
 * Manage a file based time marker.  Provides a persisted way to track the last time something was done. Once the last
 * time is returned it sets in memory the time that will be committed when set is called.
 */
public class LastTimeMarker {
    private File lastIndexTimeFile;
    private Date testDate;

    public LastTimeMarker(File dir, String name) {

        lastIndexTimeFile = new File(dir, Fmt.S("%s.lasttimemarker", name));
    }

    public long getLastIndexTimeMillis() {
        testDate = new Date();
        if (lastIndexTimeFile.exists()) {
            return lastIndexTimeFile.lastModified();
        } else {
            return -1000000000;
        }

    }

    public Date getLastIndexTime() {
        return new Date(getLastIndexTimeMillis());
    }

    public boolean set() {
        try {
            FileUtil.writeLongValToFile(lastIndexTimeFile, testDate.getTime());
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        return true;
    }
}
