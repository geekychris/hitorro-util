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
package com.hitorro.util.typesystem;

import com.hitorro.util.core.valuemap.DomainValueIntf;
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface HTObjectInputStream {
    void close() throws IOException;

    /**
     * Clear all references to objects (used in long serialization chains of self contained objects....
     */
    void clearIdTracker();


    boolean ignoreInlineContent();

    int getNonCommittedObjectCount();

    void flushRefs();

    void rollback();

    void commit();

    HTSerializable readVersionedObject()
            throws IOException, ClassNotFoundException, StoreException;


    HTSerializable readBaseTypeFromSerialzation(String guid)
            throws IOException, ClassNotFoundException, StoreException;

    String readStringInternalized()
            throws IOException, ClassNotFoundException;

    /**
     * @return
     */
    String readString()
            throws IOException, ClassNotFoundException;

    Date readDate()
            throws IOException, ClassNotFoundException;

    int readVersion()
            throws IOException;

    int readInt()
            throws IOException;

    short readShort()
            throws IOException;

    byte readByte()
            throws IOException;

    long readLong()
            throws IOException;

    boolean readBoolean() throws IOException;

    void skipNBytes(long bytesToSkip) throws IOException;

    /**
     * Get an input stream wrapper that only returns nBytes of content.
     *
     * @param bytes
     * @return constrained input stream
     */
    InputStream getLimitedInputStream(long bytes);

    HTSerializable getBaseObject(String guid);

    String[] readArrayOfStrings() throws IOException, ClassNotFoundException;

    List<String> readListOfStrings() throws IOException, ClassNotFoundException;

    HTSerializable[] readArrayOfHTSerializable()
            throws IOException, ClassNotFoundException, StoreException;

    Map<String, HTSerializable> readStringToHTSerializable()
            throws IOException, StoreException, ClassNotFoundException;

    /**
     * read a listFiles of base objects into an array.
     *
     * @param l
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void readListOfHTSerializable(List l)
            throws IOException, ClassNotFoundException, StoreException;

    List readListOfHTSerializable()
            throws IOException, ClassNotFoundException, StoreException;

    void readSetOfHTSerializable(Set set)
            throws IOException, ClassNotFoundException, StoreException;

    void readSetOfDomainValue(Set<DomainValueIntf> set)
            throws IOException, ClassNotFoundException, StoreException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;
}

