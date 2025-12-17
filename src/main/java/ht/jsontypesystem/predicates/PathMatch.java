package ht.jsontypesystem.predicates;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.Console;
import ht.util.core.opers.HTPredicate;
import ht.util.json.keys.BaseMappingProperty;

import java.util.Comparator;
import java.util.Date;

public class PathMatch<E> implements HTPredicate<JsonNode> {
    public static Comparator<String> stringEquals = new Comparator<String>() {
        @Override
        public int compare(final String o1, final String o2) {
            if (o1 != null && o2 != null) {
                return o1.compareTo(o2);
            }
            if (o1 == null) {
                return -1;
            }
            return 1;
        }
    };

    public static Comparator<Integer> intEquals = new Comparator<Integer>() {
        @Override
        public int compare(final Integer o1, final Integer o2) {
            return o1.compareTo(o2);
        }
    };

    public static Comparator<Date> dateEquals = new Comparator<Date>() {
        @Override
        public int compare(final Date o1, final Date o2) {
            return o1.compareTo(o2);
        }
    };

    private BaseMappingProperty<E> property;
    private E val;
    private Comparator<E> comparator;
    private String stringRep = null;

    public PathMatch(BaseMappingProperty<E> property, E val, Comparator<E> comparator) {
        this.property = property;
        this.val = val;
        this.comparator = comparator;
    }

    public boolean test(JsonNode node) {
        E ee = property.apply(node);

        return comparator.compare(ee, val) == 0;
    }

    public String toString() {
        if (stringRep == null) {
            StringBuilder sb = new StringBuilder();
            Console.bprint(sb, "{p:%s, val:%s, comp:%s}", property.toString(), val, comparator);
            stringRep = sb.toString();
        }
        return stringRep;
    }
}
