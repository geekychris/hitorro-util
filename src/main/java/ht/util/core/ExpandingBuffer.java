/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Vector;

/**
 * A cheap buffer we can keep adding characters to.
 */
public class ExpandingBuffer {
    private static final int ChunkSize = 4000;

    private int _nCharsInLast;
    private Vector _chunks;  // listFiles of char arrays, all but the last is full


    //--------------------------------------------------------------------------

    /**
     * Construct a new ExpandingBuffer
     */
    public ExpandingBuffer() {
        _chunks = new Vector();
        expand();
    }

    //--------------------------------------------------------------------------
    public int hashCode() {
        // grossly inefficient hashcode routine
        int summit = 0;
        for (int ii = 0; ii < _chunks.size(); ii++) {
            String xx = new String((char[]) _chunks.elementAt(ii));
            summit += xx.hashCode();
        }

        return summit;
    }

    //--------------------------------------------------------------------------

    /**
     * Construct a Reader on the current content of the buffer.
     *
     * @return a Reader
     */
    public Reader getReader() {
        // we will construct a buffer containing all of our current characters

        int len = (_chunks.size() - 1) * ChunkSize + _nCharsInLast;
        char[] buffer = new char[len];
        int offset = 0;
        for (int ii = 0; ii < _chunks.size() - 1; ii++) {
            char[] chunk = (char[]) _chunks.elementAt(ii);
            System.arraycopy(chunk, 0, buffer, offset, ChunkSize);
            offset += ChunkSize;
        }

        char[] chunk = (char[]) _chunks.lastElement();
        System.arraycopy(chunk, 0, buffer, offset, _nCharsInLast);

        // now make a reader on that

        return new CharArrayReader(buffer);
    }

    //--------------------------------------------------------------------------

    /**
     * Add a character to the buffer
     *
     * @param cc Character to put
     */
    public void append(char cc) {
        if (_nCharsInLast >= ChunkSize) {
            expand();
        }

        char[] lastChunk = (char[]) _chunks.lastElement();
        lastChunk[_nCharsInLast++] = cc;

        return;
    }

    //--------------------------------------------------------------------------

    /**
     * Add an array of characters to the buffer. This version assumes that the entire array is full of characters,
     * namely we call append(cs, 0, cs.length)
     *
     * @param cs Array to put (allowed to be null)
     */
    public void append(char[] cs) {
        if (cs != null) {
            append(cs, 0, cs.length);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Add an array of characters to the buffer
     *
     * @param cs     Array to put (allowed to be null)
     * @param offset Offset from the beginning of array where we start
     * @param ncopy  The number of characters to take
     */
    public void append(char[] cs, int offset, int ncopy) {
        int space = ChunkSize - _nCharsInLast;
        int remaining = (cs == null) ? 0 : ncopy;
        int off = offset;
        while (remaining > 0) {
            int nc = (remaining > space) ? space : remaining;
            char[] lastChunk = (char[]) _chunks.lastElement();
            System.arraycopy(cs, off, lastChunk, _nCharsInLast, nc);
            remaining -= nc;
            _nCharsInLast += nc;
            off += nc;
            if (remaining > 0) {
                expand();
                space = ChunkSize;
            }
        }

        return;
    }

    //--------------------------------------------------------------------------

    /**
     * Add a string to the buffer.
     *
     * @param ss String to put (may be null)
     */
    public void append(String ss) {
        if (ss != null) {
            append(ss.toCharArray());
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Expand the buffer by a single chunk
     */
    private void expand() {
        // we aren't checking to make sure that the last chunk is full...
        // our caller needs to deal with that
        char[] chunk = new char[ChunkSize];
        _chunks.add(chunk);
        _nCharsInLast = 0;

        return;
    }
}
