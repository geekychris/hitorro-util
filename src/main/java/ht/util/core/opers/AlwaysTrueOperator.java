package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

@TypeClassMetaInfo(shortTypeName = "AlwaysTrueOperator",
        isView = false,
        isPersisted = false,
        schemaVersion = LogicalNotOperator.SerializationVersion)
public class AlwaysTrueOperator<E> implements HTPredicate<E>, HTSerializable {
    public static final AlwaysTrueOperator oper = new AlwaysTrueOperator();
    public static final int SerializationVersion = 1;

    public AlwaysTrueOperator() {

    }

    public boolean initFromMap(final JsonNode map) {
        return true;
    }

    public void initForPass() {

    }

    public boolean test(E field) {
        return true;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:

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
