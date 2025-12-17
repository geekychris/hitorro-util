package ht.util.core.classes.methodanoconstraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.MethodAnnotation;
import ht.util.core.opers.HTPredicate;

/**
 *
 */
public class MethodArgCountMatches implements HTPredicate<MethodAnnotation> {
    private int count;

    public MethodArgCountMatches(int count) {
        this.count = count;

    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final MethodAnnotation ma) {
        return ma.getParameters().length == count;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}

