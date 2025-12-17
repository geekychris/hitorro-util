package ht.jsontypesystem.predicates;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.core.opers.HTPredicate;

import java.util.function.Predicate;

public class JVS2JsonPredicate implements HTPredicate<JVS> {
    private Predicate<JsonNode> predicate;

    public JVS2JsonPredicate(Predicate<JsonNode> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(final JVS jvs) {
        return predicate.test(jvs.getJsonNode());
    }

    public String toString() {
        return predicate.toString();
    }
}
