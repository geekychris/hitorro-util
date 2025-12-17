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
package ht.util.io.resourcecache.basefile;

import ht.util.basefile.filters.FileStartsEndsWith;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.file.FileFileSystem;
import ht.util.core.Env;
import ht.util.core.GenericKeyValue;
import ht.util.core.ListUtil;
import ht.util.core.Log;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalNotOperator;
import ht.util.core.string.Fmt;
import ht.util.io.resourcecache.ResourceDirectoryVersionNode;
import ht.util.io.resourcecache.ResourceToPoll;
import ht.util.json.keys.IntegerProperty;
import ht.util.json.keys.StringProperty;
import ht.util.versioning.VersionNode;
import ht.util.versioning.VersionTree;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class BaseFileResourceCache {
    private static final HTPredicate<BaseFile> s_noDots = new LogicalNotOperator(new FileStartsEndsWith(".", false, false));
    private static BaseFileResourceCache s_cache = null;
    protected IntegerProperty MaxKeep = new IntegerProperty("resourcecache.basefile.maxversions", "Number of versions to keep", 3);
    protected StringProperty Root = new StringProperty("resourcecache.basefile.root", "root of the resource cache", "bfresourcecache");
    protected Map<String, VersionTree<BaseFileResourceDirectoryVersionNode>> m_resources =
            new HashMap<String, VersionTree<BaseFileResourceDirectoryVersionNode>>();
    protected List<ResourceToPoll> resourcesToPoll = new ArrayList<ResourceToPoll>();
    protected BaseFile openResourceDir;
    private BaseFile m_rootDir;
    private BaseFile m_rootTmp;
    private int m_keepVersions = 3;

    public BaseFileResourceCache(BaseFile dataRoot, BaseFile openResourceDir) throws IOException {
        String root = Root.apply();
        this.openResourceDir = openResourceDir;
        m_rootDir = dataRoot.getChild(root);
        m_rootDir.mkdir();
        m_rootTmp = dataRoot.getChild(Fmt.S("%s.tmp", root));
        m_rootTmp.mkdir();

        m_keepVersions = MaxKeep.apply();
        scanDirsForVersions();
    }

    public synchronized static BaseFileResourceCache getCache() throws IOException {
        if (s_cache == null) {
            BaseFile root = FileFileSystem.Root.getFile(Env.getHome().getAbsolutePath());
            BaseFile openResource = FileFileSystem.Root.getFile(Env.getOpenResourceDir().getAbsolutePath());

            s_cache = new BaseFileResourceCache(root, openResource);
        }
        return s_cache;
    }

    public BaseFile getOpenResourceDir() {
        return openResourceDir;
    }

    public synchronized void add(ResourceToPoll rtp) {
        resourcesToPoll.add(rtp);
    }

    public synchronized List<ResourceToPoll> getResourcesToPoll() {
        return resourcesToPoll;
    }

    public List<GenericKeyValue> getCacheDetails() {
        List<GenericKeyValue> list = new ArrayList<GenericKeyValue>();
        Set<Map.Entry<String, VersionTree<BaseFileResourceDirectoryVersionNode>>> set = m_resources.entrySet();
        Iterator<Map.Entry<String, VersionTree<BaseFileResourceDirectoryVersionNode>>> iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VersionTree<BaseFileResourceDirectoryVersionNode>> entry = iter.next();
            String key = entry.getKey();
            VersionTree<BaseFileResourceDirectoryVersionNode> vt = entry.getValue();
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
    public BaseFileResourceContext getTempResourceContext(String resource, long major, long minor, long patch) {
        long currentTime = System.currentTimeMillis();
        return getTempResourceContextWithBuild(resource, major, minor, patch, currentTime);
    }

    public BaseFileResourceContext getTempResourceContextWithBuild(String resource, long major, long minor, long patch, long currentTime) {
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }

        String name = Fmt.S("%s/%s.%s.%s.%s", resource, major, minor, patch, currentTime);
        BaseFile dir = m_rootTmp.getChild(name);
        if (!dir.mkdir()) {
            Log.resourcecache.error("Unable to create temporary");
        }
        BaseFileResourceDirectoryVersionNode vn = new BaseFileResourceDirectoryVersionNode(this, resource, dir, major, minor, patch, currentTime);
        if (vn != null) {
            return new BaseFileResourceContext(this, resource, vn);
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
     * @throws java.io.IOException
     */
    public BaseFileResourceContext getTempResourceContextFromDirectoryWithCopy(String resource,
                                                                               long major,
                                                                               long minor,
                                                                               long patch,
                                                                               BaseFile dirIn)
            throws IOException {

        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }


        BaseFileResourceContext rc = this.getTempResourceContext(resource, major, minor, patch);
        dirIn.copyDirectory(rc.getPath(), AlwaysTrueOperator.oper);
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
    public BaseFileResourceContext getTempResourceContextFromDirectory(String resource, BaseFile dirIn) {
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }


        BaseFileResourceDirectoryVersionNode vn = new BaseFileResourceDirectoryVersionNode(resource, dirIn, dirIn.getName());
        if (vn != null) {
            return new BaseFileResourceContext(this, resource, vn);
        }
        return null;
    }

    public BaseFile getRootDir() {
        return m_rootDir;
    }

    public BaseFile getRootTmp() {
        return m_rootTmp;
    }

    private void scanDirsForVersions() throws IOException {
        BaseFile[] schemaVersions = m_rootDir.listFiles();
        if (schemaVersions == null) {
            return;
        }
        for (BaseFile f : schemaVersions) {
            String resource = f.getName();
            BaseFile resources[] = f.listFiles(s_noDots);
            if (resources != null) {
                for (BaseFile dir : resources) {
                    addSchemaVersion(dir, resource);
                }
            }
        }
    }

    private void addSchemaVersion(BaseFile dir, String resource) {
        String schemaVersion = dir.getName();
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            vt = new VersionTree();
            m_resources.put(resource, vt);
        }
        BaseFileResourceDirectoryVersionNode dvn = new BaseFileResourceDirectoryVersionNode(resource, dir, schemaVersion);
        vt.addVersion(dvn);
    }

    private void addSchemaVersion(String resource, BaseFileResourceDirectoryVersionNode dvn) {
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
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
    public BaseFileResourceDirectoryVersionNode getResource(String resource, String constraint) {
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(resource);
        if (vt == null) {
            return null;
        }
        BaseFileResourceDirectoryVersionNode node = vt.getNode(constraint);
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

    public boolean remove(BaseFileResourceDirectoryVersionNode node) throws IOException {
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(node.getResource());
        if (vt == null) {
            return false;
        }
        BaseFileResourceDirectoryVersionNode origNode = vt.getNode(node.getName());
        if (!vt.delete(node)) {
            // does not exist.
            return false;
        }
        BaseFile f = origNode.getDirectory();
        if (f.exists()) {
            f.deleteContentOfDir(true);

            notifyRemoval(node);
            return true;
        }
        return false;
    }

    private BaseFile getFileFromRSV(String resource, VersionNode node) {
        BaseFile f = m_rootDir.getChild(Fmt.S("%s/%s", resource, node.getName()));
        return f;
    }

    /**
     * Move temporary node to released node.
     *
     * @param resource
     * @param node
     * @param originalPath
     */
    boolean commit(String resource, BaseFileResourceDirectoryVersionNode node, BaseFile originalPath)
            throws IOException {
        BaseFile target = getFileFromRSV(resource, node);
        // must make sure the resource exists else the rename will fail
        BaseFile parent = target.getParent();
        parent.mkdir();
        if (target.exists()) {
            Log.resourcecache.error("Commit unable to complete as target resource already exists: %s", target.getAbsolutePath());
            return false;
        }
        if (!originalPath.renameTo(target)) {
            Log.resourcecache.error("Unable to rename %s to %s", originalPath, target);
            return false;
        }

        node.setDirectory(target);
        VersionTree<BaseFileResourceDirectoryVersionNode> vt = m_resources.get(node.getResource());
        if (vt == null) {
            return false;
        }
        node.setTemp(false);
        purgeOldVersions(vt, node);
        addSchemaVersion(resource, node);
        notifyNewNodeUnderResource(node);
        return true;
    }

    private void purgeOldVersions(VersionTree<BaseFileResourceDirectoryVersionNode> vt, BaseFileResourceDirectoryVersionNode node) throws IOException {
        List<BaseFileResourceDirectoryVersionNode> oldNodes = vt.getNodesMatchingVersion(node, this.m_keepVersions);
        if (!ListUtil.nullOrEmpty(oldNodes)) {
            for (BaseFileResourceDirectoryVersionNode n : oldNodes) {
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
    public void notifyNewNodeUnderResource(BaseFileResourceDirectoryVersionNode node) {

    }

    /**
     * Notify those who are interested that a node was removed.
     *
     * @param node
     */
    public void notifyRemoval(BaseFileResourceDirectoryVersionNode node) {

    }
}
