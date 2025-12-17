package ht.util.basefile.tools.queue.reader.serializer;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.CloseableIterator;
import ht.util.typesystem.BaseSession;

import java.io.IOException;


public interface WalkerDeserializer {
    CloseableIterator getIterator(BaseFile file, BaseSession session) throws IOException;
}
