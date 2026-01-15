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

import com.hitorro.util.basefile.fs.dfs.DFSFileSystem;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * S3 FileSystem implementation using Hadoop S3A with AWS SDK v2.
 * 
 * This implementation uses Hadoop's S3A FileSystem which provides:
 * - Modern AWS SDK v2 (actively supported, end-of-life for v1 is Dec 31, 2025)
 * - Better performance (2-5x faster than old S3N/JetS3t implementation)
 * - IAM role support (no hardcoded credentials needed)
 * - Server-side encryption (SSE-S3, SSE-KMS, SSE-C)
 * - Multipart uploads for large files
 * - Better error handling and automatic retries
 * - Transfer acceleration support
 * 
 * URI Format: s3a://bucket-name/path/to/file
 * 
 * Basic Configuration:
 * <pre>
 * fs.s3a.access.key=AKIAIOSFODNN7EXAMPLE
 * fs.s3a.secret.key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
 * fs.s3a.endpoint.region=us-east-1
 * </pre>
 * 
 * IAM Role Configuration (recommended for EC2/ECS):
 * <pre>
 * fs.s3a.aws.credentials.provider=org.apache.hadoop.fs.s3a.auth.IAMInstanceCredentialsProvider
 * </pre>
 * 
 * @see <a href="https://hadoop.apache.org/docs/r3.4.1/hadoop-aws/tools/hadoop-aws/index.html">Hadoop-AWS Documentation</a>
 */
public class HTS3FileSystem extends DFSFileSystem {

    protected String bucketName;
    protected String secretAccessKey;
    protected String accessKey;
    private String region;  // AWS region (optional)
    
    /**
     * Create S3 filesystem with explicit credentials.
     * 
     * @param bucketName S3 bucket name (without s3a:// prefix)
     * @param secretAccessKey AWS secret access key
     * @param accessKey AWS access key ID
     */
    public HTS3FileSystem(String bucketName, String secretAccessKey, String accessKey) {
        this(bucketName, secretAccessKey, accessKey, null);
    }
    
    /**
     * Create S3 filesystem with explicit credentials and region.
     * 
     * @param bucketName S3 bucket name (without s3a:// prefix)
     * @param secretAccessKey AWS secret access key
     * @param accessKey AWS access key ID
     * @param region AWS region (e.g., "us-east-1"), null for default
     */
    public HTS3FileSystem(String bucketName, String secretAccessKey, String accessKey, String region) {
        // Use s3a:// protocol (modern S3A implementation with AWS SDK v2)
        super(Fmt.S("s3a://%s", bucketName), Fmt.S("s3a://%s", bucketName));
        this.bucketName = bucketName;
        this.secretAccessKey = secretAccessKey;
        this.accessKey = accessKey;
        this.region = region;
    }

    /**
     * Configure S3 filesystem from configuration object.
     * 
     * @param config S3 configuration
     */
    public HTS3FileSystem(S3Config config) {
        this(config.bucket, config.secretAccessKey, config.accessKey, config.region);
    }

    @Override
    protected FileSystem getFileSystem() {
        FileSystem ret = null;

        try {
            URI uri = new URI(hdfsURI);
            Configuration conf = new Configuration(false);

            // S3A FileSystem implementation (Hadoop 3.4.x with AWS SDK v2)
            conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
            
            // Credentials configuration
            if (accessKey != null && secretAccessKey != null) {
                // Explicit credentials provided
                conf.set("fs.s3a.access.key", accessKey);
                conf.set("fs.s3a.secret.key", secretAccessKey);
                conf.set("fs.s3a.aws.credentials.provider", 
                        "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
                Log.util.debug("S3A using explicit credentials for bucket: %s", bucketName);
            } else {
                // Use default credential provider chain (IAM roles, env vars, etc.)
                conf.set("fs.s3a.aws.credentials.provider",
                        "com.amazonaws.auth.DefaultAWSCredentialsProviderChain");
                Log.util.info("S3A using default credential provider chain (IAM role, env vars, etc.)");
            }
            
            // Region configuration
            if (region != null && !region.isEmpty()) {
                conf.set("fs.s3a.endpoint.region", region);
                Log.util.debug("S3A region: %s", region);
            }
            
            // Performance tuning for production use
            conf.set("fs.s3a.connection.maximum", "100");  // Max HTTP connections
            conf.set("fs.s3a.threads.max", "64");          // Thread pool size
            conf.set("fs.s3a.fast.upload", "true");        // Use fast upload mechanism
            conf.set("fs.s3a.block.size", "134217728");    // 128MB block size
            
            // Multipart upload configuration (for files > 100MB)
            conf.set("fs.s3a.multipart.size", "104857600");      // 100MB per part
            conf.set("fs.s3a.multipart.threshold", "209715200"); // Start multipart at 200MB
            
            // Buffer configuration (use temp directory for staging)
            conf.set("fs.s3a.buffer.dir", Env.getTempDirectory().getAbsolutePath());
            
            // Retry configuration for network resilience
            conf.set("fs.s3a.retry.limit", "7");
            conf.set("fs.s3a.retry.interval", "500ms");
            conf.set("fs.s3a.connection.timeout", "200000");
            conf.set("fs.s3a.attempts.maximum", "10");
            
            // Optional: Enable server-side encryption (uncomment if needed)
            // conf.set("fs.s3a.server-side-encryption-algorithm", "AES256");
            // Or with KMS:
            // conf.set("fs.s3a.server-side-encryption-algorithm", "SSE-KMS");
            // conf.set("fs.s3a.server-side-encryption.key", "arn:aws:kms:region:account:key/key-id");
            
            ret = FileSystem.get(uri, conf);
            triedAndFailed = false;
            
            Log.util.info("✓ Connected to S3 bucket via S3A: %s (region: %s)", 
                         bucketName, region != null ? region : "default");
            return ret;
            
        } catch (IOException e) {
            Log.util.error("Unable to connect to S3 filesystem %s: %s", hdfsURI, e.getMessage(), e);
            triedAndFailed = true;
        } catch (URISyntaxException e) {
            Log.util.error("URI for S3 filesystem incorrectly formed %s: %s", hdfsURI, e.getMessage(), e);
            triedAndFailed = true;
        }

        return ret;
    }
    
    /**
     * Get the S3 bucket name.
     * 
     * @return bucket name (without s3a:// prefix)
     */
    public String getBucketName() {
        return bucketName;
    }
    
    /**
     * Get the configured AWS region.
     * 
     * @return AWS region or null if using default
     */
    public String getRegion() {
        return region;
    }
}
