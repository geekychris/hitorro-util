package ht.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;

/**
 *
 */
public class MatchClass implements HTPredicate<Class> {
    private Class c;

    public MatchClass(Class c) {
        this.c = c;
    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final Class aClass) {
        return c.equals(aClass);
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}
