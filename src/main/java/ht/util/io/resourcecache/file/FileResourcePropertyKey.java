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
package ht.util.io.resourcecache.file;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.FileProperty;
import ht.util.versioning.DirectoryVersionNode;

import java.io.File;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 11, 2005 Time: 8:15:26 AM
 * <p/>
 * Defaulting File property key.  First attempts to get the file from resource cache, failing that it will get it from
 * the backup config key.  This is typically usefull for cases where we may ship with a "default" value and then
 * eventually introduce a new version over the wire in the cache where it needs to automatically deploy.
 */
public class FileResourcePropertyKey extends FileProperty {
    private String m_resourceKey;
    private String m_resourceQueryString;
    private String m_resourceFileName;

    /**
     * @param resourceKey
     * @param queryString
     * @param resourceFileName - optional file name to append to the file path if retrieved from the cache
     * @param key
     * @param description
     * @param notNull
     */
    public FileResourcePropertyKey(String resourceKey, String queryString, String resourceFileName,
                                   String key, String description, boolean notNull) {
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
    public FileResourcePropertyKey(String resourceKey, String queryString, String resourceFileName,
                                   String key, String description, String defaultValue) {
        super(key, description, defaultValue);
        set(resourceKey, queryString, resourceFileName);
    }


    private void set(String resourceKey, String queryString, String resourceFileName) {
        m_resourceKey = resourceKey;
        m_resourceQueryString = queryString;
        m_resourceFileName = resourceFileName;
        defaultValue = new File(resourceFileName);
    }

    /**
     * Determine if the resource is actually in resource cache or not.
     *
     * @param map
     * @return
     */
    public boolean isInResourceCache(Map<String, String> map) {
        ResourceCache resourceCache = ResourceCache.getCache();
        DirectoryVersionNode dvn = resourceCache.getResource(this.m_resourceKey, this.m_resourceQueryString);
        return dvn != null;
    }

    public File apply(JsonNode map) {
        ResourceCache resourceCache = ResourceCache.getCache();
        DirectoryVersionNode dvn = resourceCache.getResource(this.m_resourceKey, this.m_resourceQueryString);
        if (dvn != null) {

            File f = dvn.getDirectory();
            if (StringUtil.nullOrEmptyOrBlankString(m_resourceFileName)) {
                return f;
            }
            return new File(f, m_resourceFileName);
        }
        return super.apply(map);
    }
}
