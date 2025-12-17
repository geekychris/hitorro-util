package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jul 24, 2005 Time: 9:13:40 PM
 */
public abstract class LogicalOperatorCollection<T> implements HTPredicate<T>, HTSerializable {
    public static final int SerializationVersion = 1;
    protected HTPredicate<? super T> m_constraints[];
    protected List<HTPredicate<? super T>> temp = new ArrayList();

    public LogicalOperatorCollection(HTPredicate<? super T>... constraints) {
        m_constraints = constraints;
    }

    public boolean initFromMap(final JsonNode map) {
        return true;
    }

    public void add(Collection<HTPredicate<? super T>> col) {
        for (HTPredicate<? super T> filt : col) {
            add(filt);
        }
        finalizeFilter();
    }

    public void add(HTPredicate<? super T> e) {
        temp.add(e);
    }

    public void addIfNotNull(HTPredicate<? super T> e) {
        if (e != null) {
            add(e);
        }
    }

    public void finalizeFilter() {
        m_constraints = temp.toArray(new HTPredicate[temp.size()]);
    }

    public void initForPass() {
        for (HTPredicate oper : m_constraints) {
            oper.initForPass();
        }
    }

    public abstract boolean test(T field);

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeArrayOfHTSerializable(m_constraints);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:
                // hack because I cant cast....I am sure there is a better way.
                HTSerializable[] temp = os.readArrayOfHTSerializable();
                m_constraints = new HTPredicate[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    m_constraints[i] = (HTPredicate) temp[i];
                }
        }
    }

    public int getSerializationVersion() {
        return SerializationVersion;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}