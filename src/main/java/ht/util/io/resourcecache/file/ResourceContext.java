package ht.util.io.resourcecache.file;

import ht.util.io.FileUtil;
import ht.util.io.resourcecache.ResourceCacheException;
import ht.util.io.resourcecache.ResourceDirectoryVersionNode;

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

