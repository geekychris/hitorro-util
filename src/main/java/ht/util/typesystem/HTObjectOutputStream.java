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
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 24, 2006 Time: 12:51:29 PM
 * <p/>
 * Manages serialization of an object to
 */
public interface HTObjectOutputStream {
    /**
     * Constants associated with serializing persisted objects.
     */
    byte ObjectReference = 01;
    byte ObjectSerialized = 02;
    byte ObjectNull = 03;
    byte EndOfStream = 04;

    void flush() throws IOException;

    /**
     * WHen the output stream is used for simple object serialization we dont need the id tracking to span longer than
     * the single highest level writeVersionedObject, else we will blow memory.
     */
    void clearIdTracker() throws IOException;

    /**
     * End of stream
     *
     * @throws IOException
     */
    void writeEnd()
            throws IOException;

    void writeVersionedObject(HTSerializable bt) throws IOException, StoreException;

    boolean includeContent();

    void serializeVersionedObjectReference(String guid) throws IOException;

    void serializeVersionedObject(String guid, HTSerializable bt) throws IOException, StoreException;

    /**
     * Wrapper for writing a string, this may change later on if we decide string serialization isnt correct for
     * versioning.
     *
     * @param s
     * @throws IOException
     */
    void writeString(String s) throws IOException;

    void writeVersion(int v) throws IOException;

    void writeInt(int v) throws IOException;

    void writeShort(short v) throws IOException;

    void writeByte(byte v) throws IOException;

    void writeLong(long v) throws IOException;

    void writeBoolean(boolean b) throws IOException;

    void writeListOfHTSerializable(List list) throws IOException, StoreException;

    void writeArrayOfHTSerializable(Object list[]) throws IOException, StoreException;

    void writeSetOfBaseType(Set set) throws IOException, StoreException;

    void writeSetOfDomainValue(Set<DomainValueIntf> set) throws IOException, StoreException;

    void writeArrayOfString(String list[]) throws IOException, StoreException;

    void writeListOfString(List<String> list) throws IOException, StoreException;

    void writeStringToHTSerializable(Map<String, HTSerializable> map) throws IOException, StoreException;

    /**
     * Copy the bytes of the input stream to our output stream
     *
     * @param is
     * @throws IOException
     */
    void writeInputStream(InputStream is) throws IOException;

    /**
     * Date should ultimately be replaced with a version independent mechanism
     *
     * @param date
     * @throws IOException
     */
    void writeDate(Date date) throws IOException;

    void writeFloat(float f) throws IOException;

    void writeDouble(double d) throws IOException;

    enum LoadType {
        IgnoreDupes,
        Merge,
        Replace
    }
}