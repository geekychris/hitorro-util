package ht.util.basefile.tools.queue.writer.serializer;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.StoreException;

import java.io.IOException;


/**
 * Responsible for providing a way to convertToPdf an object into a series of bytes using whatever serialization
 * required. Ontop of that it assumes that not all file formats can just be read without a terminator (the readers may
 * require some kind of end of file marker.  For that reason there is a closeout function that can be implemented to
 * take a file and append the appropriate marker before being copied out of the writers disk cache.
 */
public interface WriterWritableInterface<T> {
    void applyCloseToFile(BaseFile f) throws IOException;

    int getBytes(T t) throws IOException;

    String getExtension();

    void setExtension(String ext);

    boolean open(BaseFile f) throws IOException;

    boolean close() throws IOException;

    boolean write(T sd) throws IOException, StoreException;
}
