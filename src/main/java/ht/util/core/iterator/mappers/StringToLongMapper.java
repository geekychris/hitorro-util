package ht.util.core.iterator.mappers;

/**
 *
 */
public class StringToLongMapper extends BaseMapper<String, Long> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Long.class;
    }

    public String getDescription() {
        return "String to long mapper";
    }

    @Override
    public Long apply(final String e) {
        return Long.parseLong(e);
    }
}