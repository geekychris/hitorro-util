package ht.util.basefile.fs.dfs;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import ht.util.basefile.fs.configfactories.FileSystemConfig;
import ht.util.json.keys.StringProperty;

import java.io.IOException;

/**
 *
 */
public class HDFSPropertyFactory extends BaseFilePropertyFactory<HDFSConfig, DFSFile> {
    public static final String HDFS = "hdfs";

    public static StringProperty HdfsUriKey = new StringProperty("hdfsuri", "hdfs uri", null);
    public static StringProperty RootPathKey = new StringProperty("rootpath", "Root path", null);

    public String[] getNames() {
        return new String[]{"hdfsconfig"};
    }

    public HDFSConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        HDFSConfig dfs = new HDFSConfig();
        dfs.hdfsURI = HdfsUriKey.apply(map);
        dfs.rootPath = RootPathKey.apply(map);
        return dfs;
    }

    public String getProtocol() {
        return HDFS;
    }

    public BaseFile getBaseFileFromPath(String val) throws IOException {
        BaseFile bf = super.getBaseFileFromPath(val);
        if (bf != null) {
            return bf;
        }
        // didnt see any magic config stuff, going to assume that the FS can handle directly the url.
        DFSFileSystem prov = new DFSFileSystem(val);
        return prov.getFile("");
    }

    @Override
    public FileSystemConfig getConfigFromParts(final String[] parts) {
        HDFSConfig hConf = new HDFSConfig();
        hConf.hdfsURI = parts[0];
        hConf.rootPath = parts[1];
        return hConf;
    }
}

