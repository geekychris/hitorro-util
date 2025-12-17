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
package com.hitorro.util.core.iterator.sinks;


import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class LogOutputSink<T> extends BaseSink<T> {
    protected BaseMapper<File, Sink<T>> sinkMap;
    protected Sink<T> sink;
    protected File directory;
    protected String extension;
    protected int counter = 0;
    protected File tmpDirectory;

    protected String currentFileName;
    protected File tmpFileName;

    public LogOutputSink(BaseMapper<File, Sink<T>> sinkMap, File tmpDir, File directory, String extension) {
        this.sinkMap = sinkMap;
        this.directory = directory;
        this.tmpDirectory = tmpDir;
        this.extension = extension;
        tmpDirectory.mkdirs();
        directory.mkdirs();
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    public boolean start() throws IOException {
        tmpFileName = getNewTmpFile();
        sink = sinkMap.apply(tmpFileName);
        return sink.start();
    }

    public boolean add(T t) throws StoreException, IOException {
        if (sink != null) {
            return sink.add(t);
        }
        return false;
    }

    public boolean addList(List<T> ts) throws StoreException, IOException {
        int count = 0;
        for (T t : ts) {
            if (add(t)) {
                count++;
            }
        }
        return true;
    }

    public boolean stop() throws IOException {
        if (sink != null) {
            sink.stop();
            if (FileUtil.fileExistsAndNotEmpty(tmpFileName)) {
                File finalName = getFinalFileName();
                tmpFileName.renameTo(finalName);
                Log.util.info("Moved file %s to %s", tmpFileName, finalName);
            }
        }
        sink = null;
        return true;
    }

    public File getNewTmpFile() {
        currentFileName = Fmt.S("%s-%s.%s", System.currentTimeMillis(), counter++, this.extension);
        return new File(tmpDirectory, currentFileName);
    }

    public File getFinalFileName() {
        return new File(directory, currentFileName);
    }
}
