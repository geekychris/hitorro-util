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

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import ht.util.basefile.fs.configfactories.FileSystemConfig;
import ht.util.basefile.fs.dfs.DFSFile;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringProperty;

/**
 *
 */
public class S3PropertyFactory extends BaseFilePropertyFactory<S3Config, DFSFile> {
    public static final String S3 = "s3";

    public static StringProperty BucketKey = new StringProperty("bucketname", "bucket name", null);

    public static StringProperty SecretKey = new StringProperty("secretaccesskey", "bucket name", null);

    public static StringProperty AccessKey = new StringProperty("accesskey", "bucket name", null);

    public String[] getNames() {
        return new String[]{"s3config"};
    }

    public S3Config getInstance(final JsonNode map, final String type, final String parentPathName) {
        S3Config s3 = new S3Config();
        s3.bucket = BucketKey.apply(map);
        s3.secretAccessKey = SecretKey.apply(map);
        s3.accessKey = AccessKey.apply(map);
        return s3;
    }


    public String getProtocol() {
        return S3;
    }

    /**
     * Look for bucketName/secretAccessKey/accessKey
     *
     * @param val
     * @return
     */
    public DFSFile getBaseFileFromPath(String val) {
        String parts[] = StringUtil.tokenizeFromSingleChar(val, "/");
        if (parts == null || parts.length != 3) {
            return null;
        }
        HTS3FileSystem prov = new HTS3FileSystem(parts[0], parts[1], parts[2]);
        return prov.getFile("");
    }

    /**
     * Assumes format of:
     * <p>
     * s3://@@bucketName/secretAccessKey/accessKey@@/path
     *
     * @param parts
     * @return
     */

    public FileSystemConfig getConfigFromParts(String parts[]) {
        S3Config s3Config = new S3Config();
        s3Config.bucket = parts[0];
        s3Config.secretAccessKey = parts[1];
        s3Config.accessKey = parts[2];

        return s3Config;
    }
}