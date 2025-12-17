package ht.util.core.iterator.mappers;

/**
 *
 */
public class StringToShortMapper extends BaseMapper<String, Short> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Short.class;
    }

    public String getDescription() {
        return "String to short mapper";
    }

    @Override
    public Short apply(final String e) {
        return Short.parseShort(e);
    }
}
