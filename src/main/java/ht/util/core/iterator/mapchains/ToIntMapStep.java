package ht.util.core.iterator.mapchains;

import ht.util.core.iterator.mappers.BaseMapper;


/**
 *
 */
public class ToIntMapStep extends BaseMapper<String, Integer> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return Integer.class;
    }

    public String getDescription() {
        return "Converts a string to an integer";
    }

    @Override
    public Integer apply(final String o) {
        return Integer.parseInt(o);
    }
}
