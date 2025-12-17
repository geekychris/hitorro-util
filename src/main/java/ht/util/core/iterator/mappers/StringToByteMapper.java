package ht.util.core.iterator.mappers;

/**
 * Created by IntelliJ IDEA. User: Chris Date: 3/29/11 Time: 8:03 AM To change this template use File | Settings | File
 * Templates.
 */
public class StringToByteMapper extends BaseMapper<String, Byte> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Byte.class;
    }

    public String getDescription() {
        return "String to byte mapper";
    }

    @Override
    public Byte apply(final String e) {
        return Byte.parseByte(e);
    }
}
