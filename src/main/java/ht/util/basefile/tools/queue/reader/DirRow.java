package ht.util.basefile.tools.queue.reader;

import ht.util.basefile.fs.BaseFile;

/**
 *
 */
public class DirRow {
    BaseFile bf;
    String fileName;
    int fileAsInt;
    boolean isDir;
    DirectoryContainer container;

    public DirRow(BaseFile bf) {
        this.bf = bf;
        fileName = bf.getName();
        isDir = bf.isDir();
        fileAsInt = bf.getNameAsInt();
    }

    public String toString() {
        return bf.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof DirRow) {
            DirRow dr = (DirRow) o;
            return dr.fileName.equals(fileName);
        }
        return false;
    }
}
