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

import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.filefilters.FileStartsEndsWith;
import com.hitorro.util.io.filefilters.NotFilter;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.io.resourcecache.ResourceToPoll;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.versioning.VersionNode;
import com.hitorro.util.versioning.VersionTree;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * <p/>
 * Manages local resource files
 * <p/>
 * Structure is:
 * <p/>
 * <HOME>/resourcecache/<resourcename>/<schemaversion>/<version>
 * <p/>
 * When a new
 */
public class ResourceCache {
    private static final FilenameFilter s_noDots = new NotFilter(new FileStartsEndsWith(".", false, false));
    private static ResourceCache s_cache = null;
    protected IntegerProperty MaxKeep = new IntegerProperty("resourcecache.maxversions", "Number of versions to keep", 3);
    protected StringProperty Root = new StringProperty("resourcecache.root", "root of the resource cache", "resourcecache");
    protected Map<String, VersionTree<ResourceDirectoryVersionNode>> m_resources =
            new HashMap<String, VersionTree<ResourceDirectoryVersionNode>>();
    protected List<ResourceToPoll> resourcesToPoll = new ArrayList<ResourceToPoll>();
    private File m_rootDir;
    private File rootTmp;
    private int keepVersions = 3;

    public ResourceCache() {
        String root = Root.apply();
        m_rootDir = new File(Env.getHome(), root);
        FileUtil.ensureDirectoryExists(m_rootDir);
        rootTmp = new File(Env.getHome(), Fmt.S("%s.tmp", root));
        FileUtil.ensureDirectoryExists(rootTmp);

        keepVersions = MaxKeep.apply();
        scanDirsForVersions();
    }

    public synchronized static ResourceCache getCache() {
        if (s_cache == null) {
            s_cache = new ResourceCache();
        }
        return s_cache;
    }

    public synchronized void add(ResourceToPoll rtp) {
        resourcesToPoll.add(rtp);
    }

    public synchronized List<ResourceToPoll> getResourcesToPoll() {
        return resourcesToPoll;
    }

    public List<GenericKeyValue> getCacheDetails() {
        List<GenericKeyValue> list = new ArrayList<GenericKeyValue>();
        Set<Map.Entry<String, VersionTree<ResourceDirectoryVersionNode>>> set = m_resources.entrySet();
        Iterator<Map.Entry<String, VersionTree<ResourceDirectoryVersionNode>>> iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VersionTree<ResourceDirectoryVersionNode>> entry = iter.next();
            String key = entry.getKey();
            VersionTree<ResourceDirectoryVersionNode> vt = entry.getValue();
            vt.dumpVersionsToKeyValue(list, key);
        }
        return list;
    }

    /**
     * get a temporary resource compContext, from there we can commit this resource to the real resource tree once
     * complete.
     *
     * @param resource
     * @param major
     * @param minor
     * @param patch
     * @return
     */
    public ResourceContext getTempResourceContext(String resource, long major, long minor, long patch) {
        long currentTime = System.currentTimeMillis();
        return getTempResourceContextWithBuild(resource, major, minor, patch, currentTime);
    }

    public ResourceContext getTempResourceContextWithBuild(String resource, long major, long minor, long patch, long currentTime) {
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }

        String name = Fmt.S("%s/%s.%s.%s.%s", resource, major, minor, patch, currentTime);
        File dir = new File(this.rootTmp, name);
        if (!dir.mkdirs()) {
            Log.resourcecache.error("Unable to create temporary");
        }
        ResourceDirectoryVersionNode vn = new ResourceDirectoryVersionNode(resource, dir, major, minor, patch, currentTime);
        if (vn != null) {
            return new ResourceContext(this, resource, vn);
        }
        return null;
    }

    /**
     * Create a resourcecontext (temporary until committed) with the provided version number. Build number created by
     * current time.  Copies the provided directory content into the temporary resource compContext.
     *
     * @param resource
     * @param major
     * @param minor
     * @param patch
     * @param dirIn
     * @return
     * @throws IOException
     */
    public ResourceContext getTempResourceContextFromDirectoryWithCopy(String resource,
                                                                       long major,
                                                                       long minor,
                                                                       long patch,
                                                                       File dirIn)
            throws IOException {

        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }


        ResourceContext rc = this.getTempResourceContext(resource, major, minor, patch);
        FileUtil.copyDirectory(dirIn, rc.getPath());
        return rc;
    }

    /**
     * Allows importing into the resource cache a versioned directory.  The dirIn and the resourcecache must be on the
     * same file system, else the subsequent file rename will not work.
     * <p/>
     * Assumes the directory is of the form: <path>/major.minor.patch.build
     *
     * @param resource
     * @param dirIn
     * @return
     */
    public ResourceContext getTempResourceContextFromDirectory(String resource, File dirIn) {
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }


        ResourceDirectoryVersionNode vn = new ResourceDirectoryVersionNode(resource, dirIn, dirIn.getName());
        if (vn != null) {
            return new ResourceContext(this, resource, vn);
        }
        return null;
    }

    public File getRootDir() {
        return m_rootDir;
    }

    public File getRootTmp() {
        return rootTmp;
    }

    private void scanDirsForVersions() {
        File[] schemaVersions = m_rootDir.listFiles();
        if (schemaVersions == null) {
            return;
        }
        for (File f : schemaVersions) {
            String resource = f.getName();
            File resources[] = f.listFiles(s_noDots);
            if (resources != null) {
                for (File dir : resources) {
                    addSchemaVersion(dir, resource);
                }
            }
        }
    }

    private void addSchemaVersion(File dir, String resource) {
        String schemaVersion = dir.getName();
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }
        ResourceDirectoryVersionNode dvn = new ResourceDirectoryVersionNode(resource, dir, schemaVersion);
        vt.addVersion(dvn);
    }

    private void addSchemaVersion(String resource, ResourceDirectoryVersionNode dvn) {
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }
        vt.addVersion(dvn);
    }

    /**
     * Get a resource by query.  Increments its use count, so will be excluded from purging if a purge is called.
     * <p/>
     * Once you have finished with a resource, you should call releaseVersionNode()
     * <p/>
     * its not a big deal if release is never called, once the server is restarted, these use counts will clear up. It
     * will be a problem if there are many many versions and knobody releases the version.
     *
     * @param resource
     * @param constraint
     * @return
     */
    public ResourceDirectoryVersionNode getResource(String resource, String constraint) {
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            return null;
        }
        ResourceDirectoryVersionNode node = vt.getNode(constraint);
        if (node != null) {
            node.incrementUseCount();
        }
        return node;
    }

    /**
     * called by a user of a resource that no longer requires the resource.
     *
     * @param node
     */
    public void releaseVersionNode(ResourceDirectoryVersionNode node) {
        if (node != null) {
            node.derementUseCount();
        }
    }

    public boolean remove(ResourceDirectoryVersionNode node) {
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(node.getResource());
        if (vt == null) {
            return false;
        }
        ResourceDirectoryVersionNode origNode = vt.getNode(node.getName());
        if (!vt.delete(node)) {
            // does not exist.
            return false;
        }
        File f = origNode.getDirectory();
        if (f.exists()) {
            List<File> notDeleted = FileUtil.deleteDirectoryContent(f, true);
            if (ListUtil.nullOrEmpty(notDeleted)) {
                notifyRemoval(node);
                return true;
            }
        }
        return false;
    }

    private File getFileFromRSV(String resource, VersionNode node) {
        File f = new File(m_rootDir, Fmt.S("%s/%s", resource, node.getName()));
        return f;
    }

    /**
     * Move temporary node to released node.
     *
     * @param resource
     * @param node
     * @param originalPath
     */
    boolean commit(String resource, ResourceDirectoryVersionNode node, File originalPath)
            throws IOException {
        File target = getFileFromRSV(resource, node);
        // must make sure the resource exists else the rename will fail
        File parent = target.getParentFile();
        FileUtil.ensureDirectoryExists(parent);
        if (target.exists()) {
            Log.resourcecache.error("Commit unable to complete as target resource already exists: %s", target.getCanonicalPath());
            return false;
        }
        if (!originalPath.renameTo(target)) {
            Log.resourcecache.error("Unable to rename %s to %s", originalPath, target);
            return false;
        }

        node.setDirectory(target);
        VersionTree<ResourceDirectoryVersionNode> vt = m_resources.get(node.getResource());
        if (vt == null) {
            return false;
        }
        node.setTemp(false);
        node.ensureLink();
        purgeOldVersions(vt, node);
        addSchemaVersion(resource, node);
        notifyNewNodeUnderResource(node);
        return true;
    }

    private void purgeOldVersions(VersionTree<ResourceDirectoryVersionNode> vt, ResourceDirectoryVersionNode node) {
        List<ResourceDirectoryVersionNode> oldNodes = vt.getNodesMatchingVersion(node, this.keepVersions);
        if (!ListUtil.nullOrEmpty(oldNodes)) {
            for (ResourceDirectoryVersionNode n : oldNodes) {
                if (n.getUseCount() == 0) {
                    this.remove(n);
                }

            }
        }
    }

    /**
     * Notify anyone who cares about new versions under this resource.
     *
     * @param node
     */
    public void notifyNewNodeUnderResource(ResourceDirectoryVersionNode node) {

    }

    /**
     * Notify those who are interested that a node was removed.
     *
     * @param node
     */
    public void notifyRemoval(ResourceDirectoryVersionNode node) {

    }
}

