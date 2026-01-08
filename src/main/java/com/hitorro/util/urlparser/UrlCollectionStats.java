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
package com.hitorro.util.urlparser;


import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**

 * Utility class to collect some basic info about a set of urls.
 */
public class UrlCollectionStats {
    private UrlCursor curs = new UrlCursor();
    private List<String> urls = new ArrayList<String>();
    private TObjectIntHashMap tldFrequencyMap = null;

    // high water mark
    private int maxValue = -1;
    private String maxTld;

    public void clear() {
        tldFrequencyMap = null;
        urls.clear();
        maxValue = -1;
        maxTld = null;
    }

    public void add(String url) {
        urls.add(url);
    }

    public int getSize() {
        return urls.size();
    }

    /**
     * Count of distinct TokenKey's found in set.
     *
     * @return
     */
    public int getTLDCount() {
        computeTLDDistribution();
        return tldFrequencyMap.size();
    }

    /**
     * Number of top TokenKey occurences.
     *
     * @return count
     */
    public int getHighWaterTLDCount() {
        computeTLDDistribution();
        return maxValue;
    }

    /**
     * Top occuring TokenKey
     *
     * @return top tld.
     */
    public String getHighWaterTLD() {
        computeTLDDistribution();
        return maxTld;
    }

    private void computeTLDDistribution() {
        if (tldFrequencyMap == null) {
            tldFrequencyMap = new TObjectIntHashMap();
            for (String url : urls) {
                String tld = UrlCursor.getSiteFromURL(url);
                if (tldFrequencyMap.contains(tld)) {
                    tldFrequencyMap.increment(tld);
                } else {
                    tldFrequencyMap.put(tld, 1);
                }
            }

            TObjectIntIterator iter = tldFrequencyMap.iterator();

            while (iter.hasNext()) {
                iter.advance();
                if (iter.value() > maxValue) {
                    maxTld = (String) iter.key();
                    maxValue = iter.value();
                }

            }
        }
    }


}