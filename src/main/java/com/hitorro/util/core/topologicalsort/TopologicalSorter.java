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


import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import com.hitorro.util.core.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Dec 23, 2004 Time: 1:49:44 PM
 * <p/>
 * Description: Responsible for taking a set of nodes described using the Node interface and sorting them based upon
 * their dependencies.  it is possible that this will fail if a cycle is detected
 */
public class TopologicalSorter<T extends NodeInterface> {
    private TIntObjectHashMap<List<NodeEntry<T>>> dependents = new TIntObjectHashMap();
    private TreeMap<Object, NodeEntry<T>> nodes = new TreeMap<Object, NodeEntry<T>>();

    /**
     * Add all the nodes in one by providing them via a collection
     *
     * @param col
     */
    public void add(Collection<T> col) {
        for (T t : col) {
            add(t);
        }
    }

    /**
     * Add nodes one by one
     *
     * @param node
     */
    public void add(T node) {
        T[] nodes = (T[]) node.getDependentNodes();
        addToDependsOnList(node, null, false);

        for (T tn : nodes) {
            NodeEntry<T> ne = addToDependsOnList(node, tn, true);
        }
    }


    /**
     * Returns the listFiles of nodes in their sorted form.
     *
     * @return null if cycle found else the sorted listFiles of nodes. listFiles is provided in  order, the first elements are
     * those that dont depend on anything, but may have things depending on it.
     */
    public List<T> getSorted() {
        List<T> sorted = new ArrayList();
        for (NodeEntry<T> ne : nodes.values()) {
            int i = ne.dependents.size();
            if (i == 0) {
                sorted.add(ne.node);
            } else {
                List<NodeEntry<T>> lne = dependents.get(i);
                if (lne == null) {
                    lne = new ArrayList<NodeEntry<T>>();
                    dependents.put(i, lne);
                }
                lne.add(ne);
            }
        }
        if (sorted.size() == 0) {
            // Indicates a cycle
            Log.util.error("Found cycle during topological sort");
            return null;
        }
        int cursor = 0;
        int sortedCount = sorted.size();
        int totalNodes = nodes.size();
        while (totalNodes != sortedCount) {
            pass(sorted.get(cursor), sorted);
            cursor++;
            sortedCount = sorted.size();
        }
        return sorted;
    }

    private int pass(T source, List<T> sorted) {
        int count = 0;
        int keys[] = new int[dependents.size()];
        int i = 0;
        for (TIntObjectIterator it = dependents.iterator(); it.hasNext(); ) {
            it.advance();
            keys[i++] = it.key();
        }
        // prevent concurrent updates, do seperately
        for (int j : keys) {
            count += scanAndCascade(j, source, sorted);
        }
        return count;
    }

    /**
     * @param tier
     * @param source
     * @return
     */
    private int scanAndCascade(int tier, T source, List<T> sorted) {
        List<NodeEntry<T>> dependentTier = dependents.get(tier);
        List<NodeEntry<T>> resultList = null;
        int count = 0;
        int tierBelow = tier - 1;
        if (tierBelow != 0) {
            resultList = dependents.get(tierBelow);
        }
        if (dependentTier == null) {
            return count;
        }
        for (int i = dependentTier.size() - 1; i >= 0; i--) {
            NodeEntry<T> ne = dependentTier.get(i);
            if (ne.removeDependent(source)) {
                count++;
                if (tierBelow == 0) {
                    // has 0 nodes depending on it in the wild, put it in the "sorted" listFiles.
                    sorted.add(ne.node);
                } else {
                    if (resultList == null) {
                        resultList = new ArrayList();
                        dependents.put(tierBelow, resultList);
                    }
                    resultList.add(ne);
                }
                dependentTier.remove(i);
            }
        }
        return count;
    }

    private NodeEntry addToDependsOnList(T node, T targetNode, boolean checkTarget) {
        Object key = node.getKey();
        NodeEntry ne = nodes.get(key);
        if (ne == null) {
            ne = new NodeEntry(node, targetNode);
            nodes.put(key, ne);
        } else {
            if (targetNode != null) {
                ne.add(targetNode);
            }
        }
        if (checkTarget) {
            if (targetNode != null) {
                key = targetNode.getKey();
                ne = nodes.get(key);
                if (ne == null) {
                    ne = new NodeEntry(targetNode);
                    nodes.put(key, ne);
                }
            } else {
                Log.util.error("Missing target node");
            }
        }
        return ne;
    }
}

