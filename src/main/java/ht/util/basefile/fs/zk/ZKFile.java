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
package ht.util.basefile.fs.zk;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTException;
import ht.util.core.Log;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.io.largedata.compressedstreams.CInputStream;
import ht.util.io.largedata.compressedstreams.COutputStream;
import ht.util.io.largedata.compressedstreams.InputInputStream;
import org.apache.curator.utils.ZKPaths;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ZKFile extends BaseFile<ZKFile, ZKFileSystem> {
    private String path;
    private ZKFileSystem fs;

    private ZKFile(String path) {
        if (path == null) {
            throw new HTException("path cannot be null");
        }
        this.path = path;
    }

    public ZKFile(ZKFileSystem zkFileSystem, String path) throws HTException {
        this(path);
        this.fs = zkFileSystem;
    }


    @Override
    public long length() {
        return 0;
    }


    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public long getModifiedTime() {
        return 0;
    }

    @Override
    public ZKFile getChild(String path) {
        try {
            return new ZKFile(ZKPaths.makePath(this.path, path));
        } catch (HTException e) {
            return null;
        }
    }

    @Override
    public boolean mkParentDir() {
        return false;
    }

    @Override
    public ZKFile getParent() {
        return new ZKFile(ZKPaths.getPathAndNode(path).getPath());
    }

    @Override
    public CInputStream getCInputStream() {
        try {
            return new InputInputStream(getDataInputStream(), length());
        } catch (IOException e) {
            Log.filesystem.error("unable to get input stream %s %s %s", path, e, e);
        }
        return null;
    }

    @Override
    public boolean mkdir() {
        throw new HTException("Not Implemented");
    }

    @Override
    public boolean exists() {
        return fs.getCoordinator().exists(path);
    }

    @Override
    public boolean replace(BaseFile replacement) {
        throw new HTException("Not implemented");
    }

    @Override
    public boolean setLastModified(final long time) {
        return false;
    }

    @Override
    public boolean renameTo(BaseFile replacement) {
        throw new HTException("Not implemented");
    }

    @Override
    public String getName() {
        return ZKPaths.getNodeFromPath(path);
    }

    @Override
    public COutputStream getCOutputStream() {
        return null;
    }

    @Override
    public COutputStream getCOutputStreamAppend() {
        return null;
    }

    @Override
    public ZKFile getTempFile() {
        return null;
    }

    @Override
    public InputStream getInputStreamRaw() {
        try {
            return new ByteArrayInputStream(fs.getCoordinator().getRawContents(path));
        } catch (HTException e) {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStreamRaw() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                fs.getCoordinator().create(path, this.toByteArray());
                super.close();
            }
        };
        return baos;
    }

    @Override
    public DataOutputStream getDataOutputStreamAppend() {
        return null;
    }

    @Override
    public ZKFile getPeer(final String peer) {
        return null;
    }

    @Override
    public ZKFile getParentAux(final String part) {
        return null;
    }

    @Override
    public boolean delete() {
        try {
            fs.getCoordinator().delete(path);
        } catch (HTException e) {
            return false;
        }
        return true;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public boolean isDir() {
        return false;
    }


    @Override
    public BaseFile[] listFiles(Predicate<BaseFile> filter) {
        if (filter == null) {
            filter = AlwaysTrueOperator.oper;
        }
        if (!exists()) {
            return BaseFile.EmptyList;
        } else {
            try {
                List<String> children = fs.getCoordinator().listChildren(path);
                ArrayList<BaseFile> out = new ArrayList<>();

                for (String baseFile : children) {
                    ZKFile child = getChild(baseFile);
                    if (filter.test(child)) {
                        out.add(child);
                    }
                }
            } catch (HTException e) {
                Log.filesystem.error("Unable to list files in path %s %s %e", path, e, e);
            }
            return BaseFile.EmptyList;
        }
    }

    public void touch() {
        if (!exists()) {
            try {
                fs.getCoordinator().create(path, new byte[0]);
            } catch (HTException e) {

            }
        }
    }
}
