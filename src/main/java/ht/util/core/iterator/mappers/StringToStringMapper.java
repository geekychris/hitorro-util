package ht.util.core.iterator.mappers;

/**
 *
 */
public class StringToStringMapper extends BaseMapper<String, String> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return String.class;
    }

    public String getDescription() {
        return "String to string mapper (do nothing";
    }

    @Override
    public String apply(final String e) {
        return e;
    }
}
