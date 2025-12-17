package ht.util.core.classes.membervaranoconstraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.classes.MemberVarAnnotations;
import ht.util.core.opers.HTPredicate;


/**
 *
 */
public class MethodVarAnoMatches implements HTPredicate<MemberVarAnnotations> {
    private Class clazz;

    public MethodVarAnoMatches(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final MemberVarAnnotations ma) {
        return ma.containsAnnotation(clazz);
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}
