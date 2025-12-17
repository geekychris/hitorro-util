package ht.util.basefile.fs.jarfile;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.string.StringUtil;
import ht.util.io.largedata.compressedstreams.CInputStream;
import ht.util.io.largedata.compressedstreams.COutputStream;
import ht.util.io.largedata.compressedstreams.InputInputStream;
import ht.util.io.largedata.compressedstreams.OutputOutputStream;

import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Must be a file on the local file system, if not you must use the more limitied JarFileFile.  This basefile
 * allows slightly better manipulation of a jar file (such as enumeration)
 */
public class ZipFileFile extends BaseFile<ZipFileFile, ZipFileSystem> implements Comparable {
    private ZipFile zipFile;
    private File file;
    private ZipEntry ze;

    public ZipFileFile(File file, String path) throws IOException {
        this.file = file;
        zipFile = new ZipFile(file);
        this.path = path;
        ze = zipFile.getEntry(path);
    }

    public void touch() {
        FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
        ze.setLastModifiedTime(ft);
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        return ze != null;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public ZipFileFile getChild(final String child) {
        try {
            return new ZipFileFile(file, StringUtil.strcat(path, "/", child));
        } catch (IOException e) {
            Log.filesystem.error("Unable to create zipfilefile for %s/%s error %s %e", file, path, e, e);
            return null;
        }
    }

    @Override
    public boolean mkParentDir() {
        return false;
    }

    @Override
    public InputStream getInputStreamRaw() throws IOException {
        // control the close method else the zis gets closed prematurely (close on ZIS interacts badly with callers who
        // think its just a stream)
        return zipFile.getInputStream(ze);
    }

    @Override
    public OutputStream getOutputStreamRaw() {
        return null;
    }

    @Override
    public DataOutputStream getDataOutputStreamAppend() {
        return null;
    }

    @Override
    public ZipFileFile getPeer(final String peer) {
        return null;
    }

    public ZipFileFile getParentAux(String part) {
        return null;
    }

    @Override
    public CInputStream getCInputStream() throws IOException {
        return new InputInputStream(getDataInputStream(), length());
    }

    @Override
    public long getModifiedTime() {
        return ze.getTime();
    }

    @Override
    public COutputStream getCOutputStream() throws IOException {
        return new OutputOutputStream(getDataOutputStream());
    }

    @Override
    public COutputStream getCOutputStreamAppend() throws IOException {
        return new OutputOutputStream(getDataOutputStreamAppend());
    }

    @Override
    public ZipFileFile getTempFile() {
        return null;
    }

    /**
     * returns true if we didnt actually find the guy, you should do an exists check if you need a directory
     *
     * @return
     */
    @Override
    public boolean isDir() {
        return ze.isDirectory();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public long length() {
        return ze.getSize();
    }

    @Override
    public ZipFileFile[] listFiles() {
        return null;
    }

    @Override
    public ZipFileFile[] listFiles(final Predicate<BaseFile> filter) {
        ZipFileFile listIn[] = listFiles();
        List<ZipFileFile> list = new ArrayList();
        for (ZipFileFile zff : listIn) {
            if (filter.test(zff)) {
                list.add(zff);
            }
        }
        return list.toArray(new ZipFileFile[list.size()]);
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean renameTo(final BaseFile replaceWithMe) {
        return false;
    }

    @Override
    public boolean replace(final BaseFile replaceWithMe) {
        return false;
    }

    @Override
    public boolean setLastModified(final long time) {
        return false;
    }

    public int compareTo(final Object o) {
        if (o instanceof ZipFileFile) {
            ZipFileFile ff = (ZipFileFile) o;
            return path.compareTo(ff.path);
        }
        return -1;
    }
}
