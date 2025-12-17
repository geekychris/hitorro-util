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
package com.hitorro.util.core.topologicalsort;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.ListUtil;

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

