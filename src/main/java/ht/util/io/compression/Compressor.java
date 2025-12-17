package ht.util.io.compression;

import java.io.IOException;

/**
 *
 */
public interface Compressor {
    boolean init(int version) throws IOException;

    int compressBytes(byte[] input) throws IOException;

    int compressBytes(byte[] buff, int size) throws IOException;

    byte[] decompressBytes(byte[] input) throws IOException;

    byte[] getCompressionBuffer();

    int getVersion();
}
