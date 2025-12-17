package ht.util.core.iterator.mappers;

/**
 *
 */
public class StringToIntMapper extends BaseMapper<String, Integer> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Integer.class;
    }

    public String getDescription() {
        return "String to integer mapper";
    }

    @Override
    public Integer apply(final String e) {
        return Integer.parseInt(e);
    }
}
