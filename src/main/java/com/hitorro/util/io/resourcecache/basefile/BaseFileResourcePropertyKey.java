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
package com.hitorro.util.io.resourcecache.basefile;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.resourcecache.file.ResourceCache;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.versioning.BaseFileDirectoryVersionNode;
import com.hitorro.util.versioning.DirectoryVersionNode;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class BaseFileResourcePropertyKey extends BasefileProperty {
    private String m_resourceKey;
    private String resourceQueryString;
    private String m_resourceFileName;

    /**
     * @param resourceKey
     * @param queryString
     * @param resourceFileName - optional file name to append to the file path if retrieved from the cache
     * @param key
     * @param description
     */
    public BaseFileResourcePropertyKey(String resourceKey, String queryString, String resourceFileName,
                                       String key, String description) {
        super(key, description);

        set(resourceKey, queryString, resourceFileName);
    }

    /**
     * @param resourceKey
     * @param queryString
     * @param resourceFileName
     * @param key
     * @param description
     * @param defaultValue
     */
    public BaseFileResourcePropertyKey(String resourceKey, String queryString, String resourceFileName,
                                       String key, String description, String defaultValue) {
        super(key, description, defaultValue);
        set(resourceKey, queryString, resourceFileName);
    }


    private void set(String resourceKey, String queryString, String resourceFileName) {
        m_resourceKey = resourceKey;
        resourceQueryString = queryString;
        m_resourceFileName = resourceFileName;
    }

    /**
     * Determine if the resource is actually in resource cache or not.
     *
     * @param map
     * @return
     */
    public boolean isInResourceCache(Map<String, String> map) {
        ResourceCache resourceCache = ResourceCache.getCache();
        DirectoryVersionNode dvn = resourceCache.getResource(this.m_resourceKey, this.resourceQueryString);
        return dvn != null;
    }

    public BaseFile apply(JsonNode map) {
        BaseFileResourceCache resourceCache = null;
        try {
            resourceCache = BaseFileResourceCache.getCache();
        } catch (IOException e) {
            return null;
        }
        BaseFileDirectoryVersionNode dvn = resourceCache.getResource(this.m_resourceKey, this.resourceQueryString);
        if (dvn != null) {

            BaseFile f = dvn.getDirectory();
            if (StringUtil.nullOrEmptyOrBlankString(m_resourceFileName)) {
                return f;
            }
            return f.getChild(m_resourceFileName);
        }
        return super.apply(map);
    }
}
