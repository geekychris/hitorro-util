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
package com.hitorro.util.basefile.tools.crawler;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 * Recursively traverse a directory structure passing
 * any matching files to the callback
 */
public class BaseFileCrawler {
    private BaseFile root;
    private Sink<BaseFile> callback;
    private HTPredicate<BaseFile> oper = new AlwaysTrueOperator();

    public void setOperator(HTPredicate<BaseFile> oper) {
        this.oper = oper;
    }

    public void setRoot(BaseFile root) {
        this.root = root;
    }

    public void setCallback(Sink<BaseFile> callback) {
        this.callback = callback;
    }

    public int execute() throws IOException {
        callback.start();
        int cnt = 0;
        try {
            cnt = executePrivate(root);
        } catch (StoreException e) {
            throw new IOException(e);
        }
        callback.stop();
        return cnt;
    }

    private int executePrivate(BaseFile dir) throws IOException, StoreException {
        int count = 0;
        BaseFile bfs[] = dir.listFiles(oper);
        for (BaseFile bf : bfs) {
            if (bf.isDir()) {
                count += executePrivate(bf);
            } else {
                callback.add(bf);
                count++;
            }
        }
        return count;
    }
}
