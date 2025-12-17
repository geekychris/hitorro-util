package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;

import java.util.Set;

/**
 * User: chris
 */

public class SetLogicalOperator<E> implements HTPredicate<E> {
    private Set<E> set;

    public SetLogicalOperator(Set<E> set) {
        this.set = set;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "SetLogicalOperator.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(E e) {
        return set.contains(e);
    }
}