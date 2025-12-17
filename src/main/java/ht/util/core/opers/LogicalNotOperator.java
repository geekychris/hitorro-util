package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.Log;
import ht.util.core.string.Fmt;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jul 24, 2005 Time: 2:07:17 PM Compliment of
 * the operator
 */
@TypeClassMetaInfo(shortTypeName = "NotConstraint",
        isView = false,
        isPersisted = false,
        schemaVersion = LogicalNotOperator.SerializationVersion)
public class LogicalNotOperator<E> implements HTPredicate<E>, HTSerializable {
    public static final int SerializationVersion = 1;
    protected HTPredicate m_constraint;

    public LogicalNotOperator() {

    }

    public LogicalNotOperator(HTPredicate constraints) {
        m_constraint = constraints;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "LogicalNotOperator.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public String toString() {
        return Fmt.S("NOT(%s)", m_constraint.toString());
    }

    public boolean test(E field) {
        return !m_constraint.test(field);
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        if (!(m_constraint instanceof HTSerializable)) {
            Log.util.error("Unable to serialize NOT of class %s as it does not implement HTSerializable", m_constraint.getClass());
        }
        os.writeVersionedObject((HTSerializable) m_constraint);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:
                m_constraint = (HTPredicate) os.readVersionedObject();
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
