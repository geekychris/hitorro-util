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
package com.hitorro.util.io.filedirwatch;

import com.hitorro.util.core.Constants;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.LongFromBytesProperty;

public class DirWatcherParams {
    public static final String Watcher = "log.watcher";
    public static final BooleanProperty ZipEnabled = new BooleanProperty(Watcher + ".gz.enabled",
            "enable compression of log files",
            false);
    public static final LongFromBytesProperty MaxGZSize =
            new LongFromBytesProperty(Watcher + ".gz.maxsize",
                    "Max size in bytes of log files before, beyond which are zipped",
                    Constants.GBytes * 5);

    public static final IntegerProperty MaxZippedFiles =
            new IntegerProperty(Watcher + ".gz.maxfiles",
                    "Maximum number of log files, that beyond that which will be zipped ",
                    5);

    public static final BooleanProperty DeleteEnabled =
            new BooleanProperty(Watcher + ".delete.enabled",
                    "enable deleting of log files",
                    false);

    public static final LongFromBytesProperty DeleteMaxFileSize =
            new LongFromBytesProperty(Watcher + ".delete.maxsize",
                    "Max size in bytes of log files before, beyond which are deleted",
                    Constants.GBytes * 5);

    public static final IntegerProperty DeleteMaxFiles =
            new IntegerProperty(Watcher + ".delete.maxfiles",
                    "Max number of log files, that beyond that which will be deleted ",
                    20);


}

