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
