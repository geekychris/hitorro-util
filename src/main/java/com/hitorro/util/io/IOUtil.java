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
package com.hitorro.util.io;

import com.hitorro.util.core.iterator.LineReaderIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IOUtil {
    private static final int BufferSize = 4096;

    /**
     * Read the last n rows from the provided input stream.  This is not memory efficient as we load all rows into a list and then purge the head.
     *
     * @param limit
     * @param is
     * @return
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static List<String> getTailStringListFromFile(int limit, InputStream is) {
        List<String> list = new ArrayList();

        Iterator<String> iter = IOUtil.getLineReaderIteratorFromStream(is);
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        if (list.size() < limit) {
            return list;
        }
        return list.subList(list.size() - 1 - limit, list.size() - 1);
    }

    public static final Iterator<String> getLineReaderIteratorFromStream(
            InputStream is) {
        return new LineReaderIterator(new InputStreamReader(is));
    }

    public static final Iterator<String> getLineReaderIteratorFromStream(
            InputStream is, String encoding) throws
            UnsupportedEncodingException {
        return new LineReaderIterator(new InputStreamReader(is, encoding));
    }

    public static final InputStream getBookshelfInputStream(String a, InputStream is, String b) {
        return getBookshelfInputStream(new StringInputStream(a), is, new StringInputStream(b));
    }

    public static byte[] readByteArray(File f) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = FileUtil.getBufferedFileInputStream(f);
        IOUtil.copyStream(is, baos);
        baos.close();
        is.close();
        return baos.toByteArray();
    }

    public static final InputStream getBookshelfInputStream(InputStream... streams) {
        ArrayList ar = new ArrayList<InputStream>();
        for (InputStream is : streams) {
            ar.add(is);
        }
        return new SequenceInputStream(Collections.enumeration(ar));
    }

    /**
     * Copy input stream to output stream until no more bytes in stream.
     * <p/>
     * You must flush and close your streams yourself.
     */
    public static final void copyStream(InputStream is, OutputStream os)
            throws IOException {
        byte[] buffer = new byte[4096];
        int readBytes = is.read(buffer);
        while (readBytes != -1) {
            os.write(buffer, 0, readBytes);
            readBytes = is.read(buffer);
        }
    }

    public static final void copyStream(InputStream is, byte buffer[])
            throws IOException {
        int remaining = buffer.length;
        int pos = 0;
        int readBytes = is.read(buffer, pos, remaining);
        while (readBytes != -1 && remaining != 0) {
            pos += readBytes;
            remaining -= readBytes;
            readBytes = is.read(buffer, pos, remaining);
        }
    }

    public static final boolean copyStream(InputStream is, OutputStream os, long bytesToCopy)
            throws IOException {
        return copyStream(is, os, bytesToCopy, false);
    }

    /**
     * Copy input stream to output stream until no more bytes in stream or we have consumed the desired amount of
     * bytes.
     * <p/>
     * You must flush and close your streams yourself.
     */
    public static final boolean copyStream(InputStream is, OutputStream os, long bytesToCopy, boolean closeStreams)
            throws IOException {
        byte[] buffer = new byte[BufferSize];
        long loops = bytesToCopy / BufferSize;
        long remainder = bytesToCopy % BufferSize;
        int readBytes = 0;
        for (long i = 0; i < loops; i++) {
            readBytes = is.read(buffer);
            if (readBytes != -1) {
                // just be paranoid
                os.write(buffer, 0, readBytes);
            }
        }

        if (remainder > 0 && readBytes != -1) {
            readBytes = is.read(buffer, 0, (int) remainder);
            if (readBytes != -1) {
                // just be paranoid
                os.write(buffer, 0, readBytes);
            }
        }
        if (closeStreams) {
            is.close();
            os.flush();
            os.close();
        }
        return true;
    }

    /**
     * Take some kind of input stream and give a buffered reader.
     *
     * @param stream
     * @return Reader that is buffered
     */
    public static final BufferedReader getBufferedReader(InputStream stream) {
        InputStreamReader r = new InputStreamReader(stream);
        return new BufferedReader(r);
    }

    /**
     * Examine streams byte by byte. If the streams are end at the same byte position and all the bytes are identical,
     * then the streams are the same. length, and then byte per byte.
     *
     * @param a
     * @param b
     * @return true if streams are the same
     * @throws IOException
     */
    public static final boolean getStreamsIdentical(InputStream a,
                                                    InputStream b) throws IOException {
        int aChar;
        int bChar;
        while (true) {
            aChar = a.read();
            bChar = b.read();
            if (aChar == -1 && bChar == -1) {
                // both have hit end of stream together...they are the same
                return true;
            }
            if (aChar != bChar) {
                // either one hit end of stream before, or they have
                // different
                // bytes at this point.
                return false;
            }
        }
    }

    /**
     * Swiped from lucene serialization read a
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static final int readVInt(InputStream is) throws IOException {
        byte b = (byte) is.read();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = (byte) is.read();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    public static final void writeVInt(OutputStream os, int i) throws IOException {
        while ((i & ~0x7F) != 0) {
            os.write(((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        os.write(i);
    }
}
