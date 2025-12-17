package ht.util.core.topologicalsort;

import ht.util.core.Console;
import ht.util.core.ListUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Dec 23, 2008 Time: 1:52:56 PM
 * <p/>
 * Description: Book keeping class that represents the listFiles of nodes that depend on this
 */
class NodeEntry<T extends NodeInterface> {
    T node;
    List<T> dependents = new ArrayList();

    private String tostring = null;


    NodeEntry(T node, List<T> dependsOnMe) {
        this.node = node;
        if (!ListUtil.nullOrEmpty(dependsOnMe)) {
            int size = dependsOnMe.size();
            for (int i = 0; i < size; i++) {
                dependents.add(dependsOnMe.get(i));
            }
        }
    }

    NodeEntry(T node, T dependsOnMe) {
        this.node = node;
        if (dependsOnMe != null) {
            dependents.add(dependsOnMe);
        }
    }

    NodeEntry(T node) {
        this.node = node;
    }

    void add(T node) {

        for (NodeInterface tn : dependents) {
            if (tn.getKey().equals(node.getKey())) {
                return;
            }
        }
        dependents.add(node);
    }

    public String toString() {
        if (tostring == null) {
            StringBuilder sb = new StringBuilder();
            Console.bprint(sb, "node: [%s] ", node.getKey());
            for (NodeInterface tn : dependents) {
                Console.bprint(sb, "%s ", tn.getKey());
            }
            tostring = sb.toString();
        }
        return tostring;
    }

    public boolean removeDependent(NodeInterface node) {
        if (node == null) {
            Console.println();
        } else {
            for (int i = 0; i < dependents.size(); i++) {
                T t = dependents.get(i);
                if (t == null) {
                    Console.println();
                }
                if (t.getKey().equals(node.getKey())) {
                    dependents.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public int getDependentsRemaining() {
        return dependents.size();
    }
}

