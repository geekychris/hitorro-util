/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.jsontypesystem.predicates;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.json.keys.BaseMappingProperty;

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
