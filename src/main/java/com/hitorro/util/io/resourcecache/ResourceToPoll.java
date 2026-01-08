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
package com.hitorro.util.io.resourcecache;

import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.StringUtil;

/**
 * Defines the resource we wish to poll periodically to see if there is a newer version on another node.
 */
public class ResourceToPoll {
    private int checkEveryNMinutes;
    private long lastChecked = -1;
    private String resource;
    private String queryString;
    private String eventTopic;
    private String eventSubTopic;

    //DFIndex.ResourceName, DFIndex.VersionQuery, 1, DFIndexSingletonCacheEventKey, Cache.FlushCache

    public ResourceToPoll(String res, String q, int check, String topic, String subTopic) {
        resource = res;
        queryString = q;
        checkEveryNMinutes = check;
        eventTopic = topic;
        eventSubTopic = subTopic;
    }

    public void sendEvent() {
        if (!StringUtil.nullOrEmptyOrBlankString(getEventTopic())) {
            LocalEventHub.get().event(eventTopic, eventSubTopic, null);
        }
    }

    public int getCheckEveryNMinutes() {
        return checkEveryNMinutes;
    }

    public void setCheckEveryNMinutes(int checkEveryNMinutes) {
        this.checkEveryNMinutes = checkEveryNMinutes;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public String getEventSubTopic() {
        return eventSubTopic;
    }

    public void setEventSubTopic(String eventSubTopic) {
        this.eventSubTopic = eventSubTopic;
    }
}
