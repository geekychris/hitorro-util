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
package ht.util.versioning;

import ht.util.core.GenericKeyValue;
import ht.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 3:23:22 PM
 */
public class VersionTree<V extends VersionNode> {
    private static final OrderVersion OVComparitor = new OrderVersion();
    List<V> m_nodes = new ArrayList<V>();

    public void addVersion(V vn) {
        m_nodes.add(vn);
        sort();
    }

    public void dumpVersionsToKeyValue(List<GenericKeyValue> list, String key) {
        for (V v : m_nodes) {
            list.add(new GenericKeyValue(key, v.getName()));
        }
    }

    public boolean delete(V vn) {
        return m_nodes.remove(vn);
    }

    private void sort() {
        Collections.sort(m_nodes, OVComparitor);
    }

    /**
     * Version labels can be of the form:
     * <p/>
     * 1.2.3.234324   (test major, minor, patch build) 1.2.*          (test major minor and the latest patch and
     * build) 1.2.3+         (test major, minor and any patch that is 3 or above)
     *
     * @param version
     * @return
     */
    public V getNode(String version) {
        VersionPartComparitor[] comp = getComparitor(version);
        for (V vn : m_nodes) {
            if (vn.meetsVersionCriteria(comp)) {
                return vn;
            }
        }
        return null;
    }

    private VersionPartComparitor[] getComparitor(String version) {
        String parts[] = StringUtil.tokenizeFromSingleChar(version, ".");
        VersionPartComparitor comp[] = new VersionPartComparitor[4];
        int partsLastIndex = parts.length - 1;
        for (int i = 0; i < 4; i++) {
            if (i > partsLastIndex) {
                comp[i] = new VersionPartComparitor("*");
            } else {
                comp[i] = new VersionPartComparitor(parts[i]);
            }
        }
        return comp;
    }

    private VersionPartComparitor[] getComparitor(long major, long minor, long patch) {
        VersionPartComparitor comp[] = new VersionPartComparitor[4];
        comp[0] = new VersionPartComparitor(major);
        comp[1] = new VersionPartComparitor(minor);
        comp[2] = new VersionPartComparitor(patch);
        comp[3] = new VersionPartComparitor("*");
        return comp;
    }

    /**
     * Query for versions that test the version criteria, the ability to ignore the first n results is used so that you
     * can purge the versions when there are too many.
     *
     * @param version
     * @param startingPosition
     * @return
     */
    public List<V> getNodes(String version, int startingPosition) {
        VersionPartComparitor[] comp = getComparitor(version);
        return getNodes(comp, startingPosition);
    }


    /**
     * Get all the nodes older than a specific size, so we can do such things as purge them.
     *
     * @param vn
     * @param startingPosition
     * @return
     */
    public List<V> getNodesMatchingVersion(VersionNode vn, int startingPosition) {
        return getNodes(getComparitor(vn.getMajor(), vn.getMinor(), vn.getPatch()), startingPosition);
    }

    public List<V> getNodes(VersionPartComparitor[] comp, int startingPosition) {
        List<V> list = null;

        int curr = 0;
        for (V vn : m_nodes) {
            if (vn.meetsVersionCriteria(comp)) {
                curr++;
                if (curr >= startingPosition) {
                    if (list == null) {
                        list = new ArrayList<V>();
                    }
                    list.add(vn);
                }
            }
        }
        return list;
    }
}
