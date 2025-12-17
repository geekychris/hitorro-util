package ht.util.basefile.fs.configfactories;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.file.FileFile;
import ht.util.basefile.fs.file.FileFileSystem;
import ht.util.json.keys.StringProperty;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class FilePropertyFactory extends BaseFilePropertyFactory<FileConfig, FileFile> {
    private static final String FS = "file";
    public static StringProperty PathKey = new StringProperty("path", "path to file root", null);

    public String[] getNames() {
        return new String[]{"fileconfig"};
    }

    public FileConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        FileConfig dfs = new FileConfig();
        dfs.path = PathKey.apply(map);
        return dfs;
    }

    public String getProtocol() {
        return FS;
    }

    /**
     * Overide for file system as we dont want the fancy
     *
     * @param val
     * @return
     */
    public BaseFile getBaseFileFromPath(String val) throws IOException {
        val = JVSProperties.getProperties().resolveJsonVariable(val);

        BaseFile ff = super.getBaseFileFromPath(val);
        if (ff != null) {
            // we were using some kind of config in the path.
            return ff;
        }
        // we are just a basic file!
        File f = new File(val.substring(FS.length() + 1));
        FileFileSystem prov = new FileFileSystem(f);
        return prov.getFile("");
    }

    @Override
    public FileSystemConfig getConfigFromParts(final String[] parts) {
        return new FileConfig();
    }
}