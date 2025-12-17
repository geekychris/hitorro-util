package ht.util.core.classes.methodanoconstraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.MethodAnnotation;
import ht.util.core.opers.HTPredicate;

/**
 *
 */
public class MethodNameMatches implements HTPredicate<MethodAnnotation> {

    private String name;
    private boolean ignoreCase;

    public MethodNameMatches(String name, boolean ignoreCase) {
        this.name = name;
        this.ignoreCase = ignoreCase;

    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final MethodAnnotation ma) {
        String n = ma.getMethod().getName();
        if (ignoreCase) {
            return n.equalsIgnoreCase(name);
        }
        return n.equals(name);
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}
