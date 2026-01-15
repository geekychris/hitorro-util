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

import com.hitorro.util.basefile.fs.configfactories.FileSystemConfig;

/**
 * Configuration object for S3 filesystem.
 * 
 * Example usage:
 * <pre>
 * S3Config config = new S3Config();
 * config.bucket = "my-bucket";
 * config.accessKey = "AKIAIOSFODNN7EXAMPLE";
 * config.secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
 * config.region = "us-east-1";  // optional
 * 
 * HTS3FileSystem fs = config.getFileSystem();
 * </pre>
 */
public class S3Config extends FileSystemConfig<HTS3FileSystem> {
    /** S3 bucket name (without s3a:// prefix) */
    public String bucket;
    
    /** AWS secret access key */
    public String secretAccessKey;
    
    /** AWS access key ID */
    public String accessKey;
    
    /** AWS region (e.g., "us-east-1"), optional */
    public String region;

    public HTS3FileSystem getFileSystem() {
        return new HTS3FileSystem(this);
    }
}
