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
package com.hitorro.util.io.compression;

import gnu.trove.map.hash.TIntObjectHashMap;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.CompressedStreamUtil;
import com.hitorro.util.json.keys.FileProperty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.*;

/**
 *
 */
public class DictCompressor implements Compressor {
    public static final FileProperty DictPath = new FileProperty("compression.dictionary.dir", "", "${HT_DATA}/compression_dictionaries");
    private int version;
    private DictVersion thisVersion;
    private TIntObjectHashMap<DictVersion> dictVersion = new TIntObjectHashMap<>();
    private byte curr[] = new byte[2048];

    public DictVersion getDictVersion(int version) {
        DictVersion dv = dictVersion.get(version);
        if (dv == null) {
            dv = new DictVersion();
            try {
                dv.init(version);
            } catch (IOException e) {
                e.printStackTrace();
            }
            dictVersion.put(version, dv);
        }
        return dv;
    }


    public int getVersion() {
        return version;
    }


    public boolean init(int version) throws IOException {
        this.version = version;
        this.thisVersion = getDictVersion(version);
        return true;
    }

    public int compressBytes(byte[] input, ByteArrayOutputStream baos) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setDictionary(this.thisVersion.dict);
        baos.reset();
        IOUtil.writeVInt(baos, input.length);
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, compressor);
        compressor.finish();
        dos.write(input);

        dos.close();
        baos.close();
        return baos.size();
    }

    public int compressBytes(byte[] buff) {
        return compressBytes(buff, buff.length);
    }

    /**
     * Need to keep a buffer around that grows.  constructing a deflator stream is way too slow, seems to cause huge GC
     * issues.
     *
     * @param buff
     * @param size
     * @return
     */
    public int compressBytes(byte[] buff, int size) {
        // write the version


        int ret = 0;
        Deflater compressor = thisVersion.compressor;
        do {
            compressor.reset();
            if (curr.length < buff.length) {
                curr = new byte[buff.length * 2];
            }
            int start = writeVInt(0, version, curr);
            start = writeVInt(start, size, curr);
            //compressor.setStrategy(Deflater.BEST_COMPRESSION);
            compressor.setInput(buff);
            compressor.finish();
            ret = compressor.deflate(curr, start, curr.length - start);

            if (ret == 0) {
                boolean needsInput = compressor.needsInput();
                curr = new byte[curr.length * 2];
            } else {
                return ret + start;
            }
        }
        while (true);
    }

    public byte[] getCompressionBuffer() {
        return curr;
    }

    public byte[] decompressBytes(byte[] input) throws IOException {
        return decompressBytes(input, input.length);
    }

    /**
     * Bytes are of the format:
     * <p>
     * varInt version
     * varInt size
     * bytes []
     *
     * @param input
     * @param inputLength
     * @return
     * @throws IOException
     */
    public byte[] decompressBytes(byte[] input, int inputLength) throws IOException {
        Inflater decompresser = new Inflater();
        CInputStream ris = CompressedStreamUtil.getInputStreamFromByteArray(input);
        int version = ris.readVInt();
        int size = ris.readVInt();
        int start = (int) ris.getFilePointer();
        byte result[] = new byte[size];
        decompresser.setInput(input, start, inputLength - start);

        try {
            int s = decompresser.inflate(result);
            if (decompresser.needsDictionary()) {
                byte[] dict = getDictVersion(version).dict;
                decompresser.setDictionary(dict);
                s = decompresser.inflate(result);
            }
            //decompresser.finished();
            return result;
        } catch (DataFormatException e) {
            throw new IOException(e);
        }
    }


    private final int writeVInt(int index, int i, byte buff[]) {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) | 0x80), buff, index);
            index++;
            i >>>= 7;
        }
        writeByte((byte) i, buff, index++);
        return index;
    }

    public final void writeByte(byte b, byte buff[], int index) {

        buff[index] = b;
    }
}

class DictVersion {
    int version;
    File file;
    byte[] dict;

    // retained buffer (pool)
    Deflater compressor;

    public void init(int version) throws IOException {
        this.version = version;
        File dir = DictCompressor.DictPath.apply();
        file = new File(dir, Fmt.S("%s.txt", version));
        if (FileUtil.nullOrNotExist(file)) {
            throw new IOException(Fmt.S("dictionary file %s does not exist", file));
        }
        dict = IOUtil.readByteArray(file);
        if (dict == null || dict.length == 0) {
            throw new IOException(Fmt.S("Dictionary is missing or null %s", file));
        }
        Adler32 a32 = new Adler32();
        a32.update(dict);
        Log.compression.info("Loaded dictionary %s with adler value of %s", version, a32.getValue());

        compressor = getDeflator();
    }

    public Deflater getDeflator() {
        Deflater compressor = new Deflater();
        compressor.setDictionary(dict);
        return compressor;
    }

}