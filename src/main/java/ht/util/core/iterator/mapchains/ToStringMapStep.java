package ht.util.core.iterator.mapchains;

import ht.util.core.iterator.mappers.BaseMapper;


/**
 * ht.util.core.iterator.mapchains.ToStringMapStep
 */
public class ToStringMapStep extends BaseMapper<Object, String> {
    public Class inputType() {
        return Object.class;
    }

    public Class outputType() {
        return String.class;
    }

    public String getDescription() {
        return "Converts whatever object into a string";
    }

    @Override
    public String apply(final Object o) {
        return o.toString();
    }
}
