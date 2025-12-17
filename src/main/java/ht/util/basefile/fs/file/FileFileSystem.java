package ht.util.basefile.fs.file;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.basefile.fs.configfactories.FileConfig;
import ht.util.core.ListUtil;
import ht.util.core.string.Fmt;
import ht.util.io.FileUtil;

import java.io.File;
import java.util.List;

/**
 * User: chris
 */
public class FileFileSystem extends BaseFileSystem<FileFile, FileFileSystem> {
    /**
     * For a file system you want from /
     */
    public static FileFileSystem Root = new FileFileSystem(new File("/"));

    private File root;

    public FileFileSystem(FileConfig conf) {
        root = new File(conf.getPath());
    }

    public FileFileSystem(File basePath) {
        this.root = basePath;
        this.pathPart = "";
    }

    public String toString() {
        return Fmt.S("FileFileSystem path: %s", root.getAbsolutePath());
    }

    public FileFile getFile(String path) {
        return new FileFile(this, path);
    }

    public FileFile getFile(BaseFile af) {
        return new FileFile(this, af.getRelativePath());
    }

    public FileFile getFileEnsuringDir(String path) {
        FileFile af = new FileFile(this, path);
        File f = af.getJavaFile();
        FileUtil.ensureParentDirectories(f, true);
        return af;
    }

    public boolean deleteFileSystem() {
        List<File> errorList = FileUtil.deleteDirectoryContent(this.root, false);
        return ListUtil.nullOrEmpty(errorList);
    }

    File getFileRoot() {
        return root;
    }
}
