package ht.util.typesystem;

import ht.util.core.valuemap.DomainValueIntf;
import ht.util.io.StoreException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 24, 2006 Time: 2:03:50 PM
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

