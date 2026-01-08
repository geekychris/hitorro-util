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

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;

/**
 * 10:10:06 AM
 */
public class DirWatcherUtil {
    public static final DirectoryWatch getWatcherFromParams() {
        DirectoryWatch delete = null;
        if (Env.getArchiveLogDir() == null) {
            Log.util.error("Unable to get the archive directory for this server");
            return null;
        }
        if (DirWatcherParams.DeleteEnabled.apply()) {
            String ext;
            if (DirWatcherParams.ZipEnabled.apply()) {
                ext = "gz";
            } else {
                ext = "log";
            }
            delete = new DirectoryWatch(Env.getArchiveLogDir(), ext,
                    DirWatcherParams.DeleteMaxFiles.apply(),
                    DirWatcherParams.DeleteMaxFileSize.apply(),
                    new DeleteTask(),
                    null,
                    false);
        }
        if (DirWatcherParams.ZipEnabled.apply()) {
            return new DirectoryWatch(Env.getArchiveLogDir(), "log",
                    DirWatcherParams.MaxZippedFiles.apply(),
                    DirWatcherParams.MaxGZSize.apply(),
                    new ZipTask(true),
                    delete,
                    false);
        } else {
            return delete;
        }
    }
}
