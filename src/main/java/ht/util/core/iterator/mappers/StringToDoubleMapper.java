package ht.util.core.iterator.mappers;

/**
 *
 */
public class StringToDoubleMapper extends BaseMapper<String, Double> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Double.class;
    }

    public String getDescription() {
        return "String to double mapper";
    }

    @Override
    public Double apply(final String e) {
        return Double.parseDouble(e);
    }
}