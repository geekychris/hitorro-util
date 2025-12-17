package ht.util.json.visitors;

import ht.util.core.iterator.JsonValueSource;

import java.io.IOException;

/**
 *
 */
public interface JSONVisitor {
    /**
     * @param elem
     */
    void visit(JsonValueSource elem, int depth) throws IOException;

    /**
     * Visiting an named element, such as a apply entry
     *
     * @param elem
     */
    void visit(JsonValueSource elem, String name, int depth);
}
