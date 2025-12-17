package ht.util.basefile.fs;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import ht.util.basefile.fs.configfactories.FilePropertyFactory;
import ht.util.basefile.fs.dfs.DFSFileSystem;
import ht.util.basefile.fs.dfs.HDFSPropertyFactory;
import ht.util.basefile.fs.file.FileFileSystem;
import ht.util.basefile.fs.ftp.FTPPropertyFactory;
import ht.util.basefile.fs.s3.S3PropertyFactory;
import ht.util.core.string.Fmt;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.FileProperty;
import ht.util.json.keys.StringProperty;
import ht.util.propertykeys.complex.ComplexPropertyContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * User: chris
 */
public abstract class BaseFileSystem<F extends BaseFile, S extends BaseFileSystem> {
    public static final StringProperty HDFSNameSpace = new StringProperty("filesystem.subspace", "namespace within HDFS for data", "ht");
    public static final BooleanProperty HDFSEnabled = new BooleanProperty("filesystem.enabled", "namespace within HDFS for data", false);

    public static final FileProperty FakeHDFS = new FileProperty("filesystem.fake.dir", "Fake location for filesystem using the local FS", "");

    private static HashMap<String, ProtocolAdapter> adapters = getInitialAdapters();

    private static boolean configFactories = getInitializeConfigFactories();
    protected String pathPart;

    private static HashMap<String, ProtocolAdapter> getInitialAdapters() {
        HashMap<String, ProtocolAdapter> a = new HashMap();
        registerFS(a, new S3PropertyFactory());
        registerFS(a, new HDFSPropertyFactory());
        registerFS(a, new FTPPropertyFactory());
        registerFS(a, new FilePropertyFactory());
        return a;
    }

    private static boolean getInitializeConfigFactories() {
        ComplexPropertyContext.add(new S3PropertyFactory());
        ComplexPropertyContext.add(new HDFSPropertyFactory());
        ComplexPropertyContext.add(new FTPPropertyFactory());
        return true;
    }

    private static boolean registerFS(HashMap<String, ProtocolAdapter> a, BaseFilePropertyFactory factory) {
        ComplexPropertyContext.add(factory);
        a.put(factory.getProtocol(), factory);
        return true;
    }

    public static void addProtocolAdapter(ProtocolAdapter pa) {
        adapters.put(pa.getProtocol().toLowerCase(), pa);
    }

    public static ProtocolAdapter getProtocolAdapter(String name) {
        int index = name.indexOf(":");
        if (index != -1) {
            name = name.substring(0, index);
        }
        name = name.toLowerCase();
        return adapters.get(name);
    }

    public static BaseFile getBaseFileFromPath(final String val) throws IOException {
        ProtocolAdapter pa = getProtocolAdapter(val);
        if (pa != null) {
            return pa.getBaseFileFromPath(val);
        }
        // relative path
        BaseFileSystem prov = getGlobalNamespaceFileSystem("");
        return prov.getFile(val);
    }

    public static BaseFile getFileFromPath(StringProperty propKey, JsonNode props) throws IOException {
        String val = propKey.apply(props);
        return getBaseFileFromPath(val);
    }

    /**
     * The system or "cluster" of nodes may share some kind of shared file system.  This could be KFS, HDFS, FTP, NFS, whatever.
     * The system is configured with a "default" system if you provide a file path that is relative wherever we want to construct
     * a base file.
     *
     * @param hdfsRootPath
     * @return
     */
    public static BaseFileSystem getGlobalNamespaceFileSystem(String hdfsRootPath) {
        if (FakeHDFS.apply() != null) {
            return new FileFileSystem(new File(FakeHDFS.apply(), hdfsRootPath));
        }
        return new DFSFileSystem(Fmt.S("/%s/%s", HDFSNameSpace.apply(), hdfsRootPath));
    }

    public static FileSystemCascadingContainer getNamespaceProviderContainer(String hdfsRootPath, File fileSystemRootPath) {
        BaseFileSystem providers[] = {getGlobalNamespaceFileSystem(hdfsRootPath), new FileFileSystem(fileSystemRootPath)};
        FileSystemCascadingContainer ioc = new FileSystemCascadingContainer(providers);
        return ioc;
    }

    public abstract boolean deleteFileSystem() throws IOException;

    public abstract F getFile(BaseFile af);

    public abstract F getFile(String path);

    public abstract F getFileEnsuringDir(String path);

    // *********** HELPER METHODS FOR GETTING FILESYSTEMS *****************

    public F getFileIfExists(String path) {
        F af = getFile(path);
        if (af != null) {
            if (af.exists()) {
                return af;
            }
        }
        return null;
    }

    public String getPathPart() {
        return pathPart;
    }

    public boolean isLocalFileSystem() {
        return true;
    }
}
