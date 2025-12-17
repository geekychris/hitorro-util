package ht.util.core.iterator;

import ht.util.basefile.fs.CompressionType;
import ht.util.core.Log;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File version of the JarFile Iterator.  There is a BaseFile version (JarFileFile
 */
public class JarFileIterator extends AbstractIterator<String> {
    private ZipInputStream zis;
    private ZipEntry ze;
    private boolean nextCalled = false;

    public JarFileIterator(File jar) {
        try {
            CompressionType ct = CompressionType.getFilterByName(FileUtil.getFileExtension(jar));
            zis = new ZipInputStream(ct.getInputCompressed(FileUtil.getDataInputStreamForFile(jar)));
            nextAux();
        } catch (IOException e) {
            Log.filesystem.error("Unable to initialize JarItemIterator for file %s %e %s", e, e, jar);
        }
    }

    private void nextAux() throws IOException {
        ze = zis.getNextEntry();
        nextCalled = false;
    }

    @Override
    public void close() throws Exception {
        zis.close();
    }

    @Override
    public boolean hasNext() {
        if (nextCalled) {
            try {
                nextAux();
            } catch (IOException e) {
                Log.filesystem.error("Unable to perform next on jar file %s %e", e, e);
                return false;
            }
        }
        return ze != null;
    }

    @Override
    public String next() {
        nextCalled = true;
        return ze.getName();
    }

    @Override
    public void remove() {
    }
}
