package ht.util.basefile.fs;

import ht.util.core.iterator.Mapper;

import java.io.IOException;

public class FileInfoMapper implements Mapper<BaseFile, FileInfo> {
    public static FileInfoMapper me = new FileInfoMapper();

    @Override
    public FileInfo apply(final BaseFile baseFile) {
        FileInfo fi = null;
        try {
            fi = new FileInfo(baseFile);
        } catch (IOException e) {
            return null;
        }
        return fi;
    }
}