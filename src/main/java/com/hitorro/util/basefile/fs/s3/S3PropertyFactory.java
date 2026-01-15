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
package com.hitorro.util.basefile.fs.s3;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import com.hitorro.util.basefile.fs.configfactories.FileSystemConfig;
import com.hitorro.util.basefile.fs.dfs.DFSFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;

/**
 * Protocol adapter factory for S3 filesystem.
 * 
 * Supports both legacy s3:// and modern s3a:// protocols.
 * The s3a:// protocol uses Hadoop's S3A implementation with AWS SDK v2.
 */
public class S3PropertyFactory extends BaseFilePropertyFactory<S3Config, DFSFile> {
    // Support both old s3:// and new s3a:// protocols
    public static final String S3 = "s3";
    public static final String S3A = "s3a";

    public static StringProperty BucketKey = new StringProperty("bucketname", "bucket name", null);
    public static StringProperty SecretKey = new StringProperty("secretaccesskey", "secret access key", null);
    public static StringProperty AccessKey = new StringProperty("accesskey", "access key", null);
    public static StringProperty RegionKey = new StringProperty("region", "AWS region", null);

    public String[] getNames() {
        return new String[]{"s3config"};
    }

    public S3Config getInstance(final JsonNode map, final String type, final String parentPathName) {
        S3Config s3 = new S3Config();
        s3.bucket = BucketKey.apply(map);
        s3.secretAccessKey = SecretKey.apply(map);
        s3.accessKey = AccessKey.apply(map);
        s3.region = RegionKey.apply(map);
        return s3;
    }

    /**
     * Primary protocol is s3a (modern S3A with AWS SDK v2).
     * Legacy s3:// protocol will be converted to s3a:// automatically.
     */
    public String getProtocol() {
        return S3A;  // Changed from S3 to S3A (modern implementation)
    }

    /**
     * Parse S3 path and create filesystem.
     * 
     * Supports both legacy s3:// and modern s3a:// protocols.
     * Legacy s3:// URIs will be automatically converted to s3a://.
     *
     * @param val S3 path (e.g., s3://bucket/path or s3a://bucket/path)
     * @return DFS file for the path
     */
    public DFSFile getBaseFileFromPath(String val) {
        // Support legacy s3:// protocol by converting to s3a://
        if (val.startsWith("s3://")) {
            Log.util.warn("Legacy s3:// URI detected, converting to s3a://: %s", val);
            val = val.replace("s3://", "s3a://");
        }
        
        String parts[] = StringUtil.tokenizeFromSingleChar(val, "/");
        if (parts == null || parts.length != 3) {
            return null;
        }
        HTS3FileSystem prov = new HTS3FileSystem(parts[0], parts[1], parts[2]);
        return prov.getFile("");
    }

    /**
     * Create S3Config from path parts.
     * 
     * Expected format:
     * s3a://@@bucketName/secretAccessKey/accessKey@@/path
     * or
     * s3a://@@bucketName/secretAccessKey/accessKey/region@@/path
     *
     * @param parts Path components [bucketName, secretAccessKey, accessKey] or [bucketName, secretAccessKey, accessKey, region]
     * @return S3 configuration
     */
    public FileSystemConfig getConfigFromParts(String parts[]) {
        S3Config s3Config = new S3Config();
        s3Config.bucket = parts[0];
        s3Config.secretAccessKey = parts[1];
        s3Config.accessKey = parts[2];
        
        // Optional region parameter
        if (parts.length >= 4) {
            s3Config.region = parts[3];
        }

        return s3Config;
    }
}