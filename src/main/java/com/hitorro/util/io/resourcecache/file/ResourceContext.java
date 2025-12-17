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
package com.hitorro.util.io.resourcecache.file;

import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.resourcecache.ResourceCacheException;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 8, 2006 Time: 10:00:12 PM
 */
public class ResourceContext {
    private ResourceCache m_rCache;
    private String m_resource;
    private ResourceDirectoryVersionNode m_node;
    private File m_path;


    ResourceContext(ResourceCache rc, String resource, ResourceDirectoryVersionNode node) {
        m_rCache = rc;
        m_node = node;
        m_resource = resource;

        m_path = node.getDirectory();
    }

    public ResourceDirectoryVersionNode getVersionNode() {
        return m_node;
    }

    public File getPath() {
        return m_node.getDirectory();
    }

    /**
     * Commit this resource to the resource cache.
     *
     * @throws ResourceCacheException
     */
    public void commit() throws IOException {
        m_rCache.commit(m_resource, m_node, m_path);
    }

    /**
     * Rollback the resource if this is a temporary compContext.
     */
    public void rollback() {
        delete();
    }

    /**
     * Delete the resource
     */
    void delete() {
        FileUtil.deleteDirectoryContent(this.m_path, true);
    }
}

