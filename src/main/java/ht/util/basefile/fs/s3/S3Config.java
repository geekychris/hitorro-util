package ht.util.basefile.fs.s3;

import ht.util.basefile.fs.configfactories.FileSystemConfig;

/**
 *
 */
public class S3Config extends FileSystemConfig<HTS3FileSystem> {
    public String bucket;
    public String secretAccessKey;
    public String accessKey;

    public HTS3FileSystem getFileSystem() {
        return new HTS3FileSystem(this);
    }
}
