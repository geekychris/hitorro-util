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
package com.hitorro.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
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
