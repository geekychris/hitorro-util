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
package com.hitorro.util.basefile.tools.queue.writer;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.iterator.BaseFileSelectTreeController;

/**
 *
 */
public class MergerDirectoryStep<E> extends DirectoryStep<E> {

    public MergerDirectoryStep(String fileExtensionOut,
                               Mapper<BaseFile, AbstractIterator<E>> baseFileToIterator,
                               BaseFileAccessingObjectFactory<E> factory,
                               BaseFile root, DirectoryStepInterface next,
                               boolean runInOwnThread) {
        super(baseFileToIterator, factory, root, next, runInOwnThread, fileExtensionOut);
    }

    @Override
    public boolean process() throws Exception {
        BaseFile tmpDir = root.getChild("tmp");
        tmpDir.mkdir();
        BaseFile files[] = inProcess.listFiles();
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(tmpDir, files, 5, factory, false, fileExtensionOut, true);
        BaseFile merged = controller.merge();
        if (merged != null && merged.exists()) {
            BaseFile target = this.processedData.getChild(merged.getName());
            merged.renameTo(target);
        }
        return true;
    }
}