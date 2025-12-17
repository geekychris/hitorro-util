package ht.util.basefile.fs;

import ht.util.basefile.fs.file.FileFile;
import ht.util.basefile.fs.file.FileFileSystem;

import java.io.File;

/**
 *
 */
public class FileAdapter implements ProtocolAdapter<FileFile> {

    private static final String FS = "file";

    public String getProtocol() {
        return FS;
    }

    public FileFile getBaseFileFromPath(String val) {
        File f = new File(val.substring(FS.length() + 1));
        FileFileSystem prov = new FileFileSystem(f);
        return prov.getFile("");
    }
}
