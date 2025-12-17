package ht.util.core.classes.methodanoconstraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.MethodAnnotation;
import ht.util.core.opers.HTPredicate;

/**
 *
 */
public class MethodAnnotationMatches implements HTPredicate<MethodAnnotation> {

    private Class clazz;

    public MethodAnnotationMatches(Class c) {
        clazz = c;

    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final MethodAnnotation ma) {
        return ma.containsAnnotation(clazz);
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}

