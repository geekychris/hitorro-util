package ht.util.basefile.fs.s3;

import ht.util.basefile.fs.dfs.DFSFileSystem;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.string.Fmt;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HTS3FileSystem extends DFSFileSystem {

    private String bucketName;
    private String secretAccessKey;
    private String accessKey;

    public HTS3FileSystem(String bucketName, String secretAccessKey, String accessKey) {
        super(Fmt.S("s3://%s", bucketName), Fmt.S("s3://%s", bucketName));
        this.bucketName = bucketName;
        this.secretAccessKey = secretAccessKey;
        this.accessKey = accessKey;
    }

    /**
     * Configure this guy from a file system config
     *
     * @param config
     */
    public HTS3FileSystem(S3Config config) {
        this(config.bucket, config.secretAccessKey, config.accessKey);
    }


    protected FileSystem getFileSystem() {
        FileSystem ret = null;

        try {
            URI uri = new URI(hdfsURI);

            Configuration conf = new Configuration(false);

            //conf.set("fs.s3.impl", org.apache.hadoop.fs.s3.S3FileSystem.class.getCanonicalName());
            conf.set("fs.s3.buffer.dir", Env.getTempDirectory().getAbsolutePath());
            conf.set("fs.default.name", Fmt.S("s3://%s", bucketName));

            conf.set("fs.s3.awsSecretAccessKey", secretAccessKey);
            conf.set("fs.s3.awsAccessKeyId", accessKey);
            ret = FileSystem.get(uri, conf);
            triedAndFailed = false;
            return ret;
        } catch (IOException e) {
            Log.util.error("Unable to connect to filesystem %s %s %e", hdfsURI, e, e);
            triedAndFailed = true;
        } catch (URISyntaxException e) {
            Log.util.error("URI for filesystem name server incorrectly formed %s %e, e", hdfsURI, e, e);
            triedAndFailed = true;
        }

        return ret;
    }
}
