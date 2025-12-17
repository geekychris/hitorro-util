package ht.util.core.iterator.mapchains;

import ht.util.core.iterator.mappers.BaseMapper;

/**
 *
 */
public class TrimMapStep extends BaseMapper<String, String> {
    public Class inputType() {
        return String.class;
    }

    public Class outputType() {
        return String.class;
    }

    public String getDescription() {
        return "Trims a string";
    }

    @Override
    public String apply(final String s) {
        return s.trim();
    }
}
