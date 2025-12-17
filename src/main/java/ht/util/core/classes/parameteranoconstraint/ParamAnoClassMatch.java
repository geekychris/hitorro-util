package ht.util.core.classes.parameteranoconstraint;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;

import java.lang.annotation.Annotation;

/**
 *
 */
public class ParamAnoClassMatch implements HTPredicate<Annotation> {
    private Class classToMatch;

    public ParamAnoClassMatch(Class classToMatch) {
        this.classToMatch = classToMatch;
    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final Annotation annotation) {
        Class anoType = annotation.annotationType();
        return classToMatch == anoType;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }
}
