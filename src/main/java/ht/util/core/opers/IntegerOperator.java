package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 2, 2008 Time: 7:49:03 PM
 */
public class IntegerOperator implements HTPredicate<Integer> {
    public static final byte SerializationVersion = 1;
    public static final byte Equal = 0;
    public static final byte GreaterThan = 1;
    public static final byte GreaterThanOrEqual = 2;
    public static final byte LessThan = 3;
    public static final byte LessThanOrEqual = 4;

    private byte operator = Equal;
    private int value;

    public IntegerOperator(byte operator, int value) {
        this.operator = operator;
        this.value = value;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "IntegerOperator.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(final Integer integer) {
        switch (operator) {
            case Equal:
                return integer.intValue() == value;
            case GreaterThan:
                return integer.intValue() > value;
            case GreaterThanOrEqual:
                return integer.intValue() >= value;
            case LessThan:
                return integer.intValue() < value;
            case LessThanOrEqual:
                return integer.intValue() <= value;

        }
        return false;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        os.writeByte(operator);
        os.writeInt(value);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        operator = os.readByte();
        value = os.readInt();
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
