package ht.util.core.iterator.mappers;

/**
 * Created by IntelliJ IDEA. User: Chris Date: 3/29/11 Time: 8:02 AM To change this template use File | Settings | File
 * Templates.
 */
public class StringToFloatMapper extends BaseMapper<String, Float> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Float.class;
    }

    public String getDescription() {
        return "String to float mapper";
    }

    @Override
    public Float apply(final String e) {
        return Float.parseFloat(e);
    }
}
