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
package com.hitorro.util.basefile.fs.zk;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.core.HTException;
import com.hitorro.util.zookeeper.ZKContext;

public class ZKFileSystem extends BaseFileSystem<ZKFile, ZKFileSystem> {
    ZKContext getCoordinator() {
        return ZKContext.me;
    }

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public ZKFile getFile(final BaseFile af) {
        if (af instanceof ZKFile) {
            return (ZKFile) af;
        }
        return null;
    }

    @Override
    public ZKFile getFileEnsuringDir(final String path) {
        ZKFile f = getFile(path);
        if (f != null) {
            f.mkdir();
        }
        return f;
    }

    @Override
    public ZKFile getFile(String path) {
        try {
            return new ZKFile(this, path);
        } catch (HTException e) {
            return null;
        }
    }


}