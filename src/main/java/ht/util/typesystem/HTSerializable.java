package ht.util.typesystem;

import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 24, 2006 Time: 9:27:07 PM
 */
public interface HTSerializable {

    void serialize(HTObjectOutputStream os) throws IOException, StoreException;

    void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException;

    int getSerializationVersion();

    boolean isPersisted();

    boolean hasGuid();

    boolean hasSoftGuid();

}
