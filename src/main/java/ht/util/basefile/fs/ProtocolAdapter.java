package ht.util.basefile.fs;

import java.io.IOException;

/**
 *
 */
public interface ProtocolAdapter<F extends BaseFile> {
    String getProtocol();

    BaseFile getBaseFileFromPath(String val) throws IOException;
}
