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
package com.hitorro.util.basefile.tools;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;

import java.io.IOException;
import java.util.Date;

/**
 * Created by chris on 5/23/18.
 */
public class BaseFileLastTimeMarker {
    private BaseFile lastIndexTimeFile;

    private Date testDate;

    public BaseFileLastTimeMarker(BaseFile dir, String name) {

        lastIndexTimeFile = dir.getChild("%s.lasttimemarker", name);
    }

    public long getLastIndexTimeMillis() {
        testDate = new Date();
        if (lastIndexTimeFile.exists()) {
            return lastIndexTimeFile.getModifiedTime();
        } else {
            return -1000000000;
        }
    }

    public Date getLastIndexTime() {
        return new Date(getLastIndexTimeMillis());
    }

    public boolean set() {
        try {
            lastIndexTimeFile.writeLong(testDate.getTime());
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        return true;
    }
}