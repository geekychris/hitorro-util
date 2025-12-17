package ht.util.basefile.fs.jarfile;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 */
public class JarItemIterator extends AbstractIterator<BaseFile> {
    private ZipInputStream zis;
    private ZipEntry ze;
    private boolean nextCalled = false;

    public JarItemIterator(BaseFile jar) {
        try {
            zis = new ZipInputStream(jar.getCompressionType().getInputCompressed(jar.getDataInputStream()));
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
    public void close() throws IOException {
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
    public BaseFile next() {
        nextCalled = true;
        JarFileFile jff = new JarFileFile(zis, ze);
        ze = null;
        return jff;
    }

    @Override
    public void remove() {
    }
}
