package ht.util.urlparser;


import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
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