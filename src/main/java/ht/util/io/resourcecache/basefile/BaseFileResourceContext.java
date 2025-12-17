package ht.util.io.resourcecache.basefile;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.resourcecache.ResourceCacheException;

import java.io.IOException;

/**
 *
 */
public class BaseFileResourceContext {
    private BaseFileResourceCache m_rCache;
    private String m_resource;
    private BaseFileResourceDirectoryVersionNode m_node;
    private BaseFile m_path;


    BaseFileResourceContext(BaseFileResourceCache rc, String resource, BaseFileResourceDirectoryVersionNode node) {
        m_rCache = rc;
        m_node = node;
        m_resource = resource;

        m_path = node.getDirectory();
    }

    public BaseFileResourceDirectoryVersionNode getVersionNode() {
        return m_node;
    }

    public BaseFile getPath() {
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
    public void rollback() throws IOException {
        delete();
    }

    /**
     * Delete the resource
     */
    void delete() throws IOException {
        this.m_path.deleteContentOfDir(true);
    }
}
