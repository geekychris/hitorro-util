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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.net.URI;

/**
 * S3-compatible filesystem for services like MinIO, Wasabi, DigitalOcean Spaces, etc.
 * 
 * <p>This class extends {@link HTS3FileSystem} to support S3-compatible services that:
 * <ul>
 *   <li>Use custom endpoints (not AWS S3)</li>
 *   <li>Require path-style access (bucket.endpoint vs endpoint/bucket)</li>
 *   <li>May use HTTP instead of HTTPS (for local development)</li>
 * </ul>
 * 
 * <h2>Supported Services</h2>
 * <ul>
 *   <li><b>MinIO</b> - Self-hosted S3-compatible object storage</li>
 *   <li><b>Wasabi</b> - Cloud object storage (wasabi.com)</li>
 *   <li><b>DigitalOcean Spaces</b> - Cloud object storage</li>
 *   <li><b>Backblaze B2</b> - Cloud storage with S3-compatible API</li>
 *   <li><b>Cloudflare R2</b> - Zero-egress cloud storage</li>
 *   <li>Any other S3-compatible service</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>MinIO (Local Development)</h3>
 * <pre>
 * S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(
 *     "http://localhost:9000",  // endpoint
 *     "my-bucket",              // bucket
 *     "minioadmin",             // access key
 *     "minioadmin",             // secret key
 *     false                     // SSL disabled for local
 * );
 * 
 * BaseFile file = s3.getFile("path/to/file.txt");
 * try (OutputStream os = file.getOutputStream()) {
 *     os.write("Hello MinIO!".getBytes());
 * }
 * </pre>
 * 
 * <h3>Wasabi (Production)</h3>
 * <pre>
 * S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(
 *     "https://s3.us-east-2.wasabisys.com",  // endpoint
 *     "my-bucket",                            // bucket
 *     "AKIAIOSFODNN7EXAMPLE",                 // access key
 *     "wJalrXUtnFEMI/K7MDENG/bPxRfiCY...",   // secret key
 *     true                                    // SSL enabled
 * );
 * </pre>
 * 
 * <h3>DigitalOcean Spaces</h3>
 * <pre>
 * S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(
 *     "https://nyc3.digitaloceanspaces.com",
 *     "my-space",
 *     "DO00EXAMPLE",
 *     "secret...",
 *     true
 * );
 * </pre>
 * 
 * <h3>Configuration via S3Config</h3>
 * <pre>
 * S3Config config = new S3Config();
 * config.bucket = "my-bucket";
 * config.accessKey = "AKIAIOSFODNN7EXAMPLE";
 * config.secretAccessKey = "wJalrXUtnFEMI...";
 * 
 * // For S3-compatible services, use S3CompatibleFileSystem
 * S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(
 *     "https://s3.wasabisys.com",
 *     config.bucket,
 *     config.accessKey,
 *     config.secretAccessKey,
 *     true  // SSL enabled
 * );
 * </pre>
 * 
 * <h2>Configuration Options</h2>
 * 
 * The filesystem can be customized by overriding {@link #configureFileSystem(Configuration)}:
 * 
 * <pre>
 * S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(...) {
 *     {@literal @}Override
 *     protected void configureFileSystem(Configuration conf) {
 *         super.configureFileSystem(conf);
 *         // Custom settings
 *         conf.set("fs.s3a.connection.maximum", "100");
 *         conf.set("fs.s3a.threads.max", "64");
 *     }
 * };
 * </pre>
 * 
 * @see HTS3FileSystem
 * @see S3Config
 */
public class S3CompatibleFileSystem extends HTS3FileSystem {
    
    private final String endpoint;
    private final boolean sslEnabled;
    
    /**
     * Create S3-compatible filesystem with custom endpoint.
     * 
     * @param endpoint The S3-compatible endpoint URL (e.g., "http://localhost:9000" for MinIO,
     *                 "https://s3.wasabisys.com" for Wasabi)
     * @param bucketName The bucket/space name
     * @param accessKey The access key ID
     * @param secretAccessKey The secret access key
     * @param sslEnabled Whether to use SSL/TLS (true for production, false for local development)
     */
    public S3CompatibleFileSystem(String endpoint, String bucketName, 
                                   String accessKey, String secretAccessKey, 
                                   boolean sslEnabled) {
        super(bucketName, secretAccessKey, accessKey);
        this.endpoint = endpoint;
        this.sslEnabled = sslEnabled;
    }
    
    /**
     * Create S3-compatible filesystem with custom endpoint (SSL enabled by default).
     * 
     * @param endpoint The S3-compatible endpoint URL
     * @param bucketName The bucket/space name
     * @param accessKey The access key ID
     * @param secretAccessKey The secret access key
     */
    public S3CompatibleFileSystem(String endpoint, String bucketName, 
                                   String accessKey, String secretAccessKey) {
        this(endpoint, bucketName, accessKey, secretAccessKey, true);
    }
    
    @Override
    protected FileSystem getFileSystem() {
        try {
            URI uri = new URI(String.format("s3a://%s", getBucketName()));
            Configuration conf = new Configuration(false);
            
            // S3A implementation
            conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
            
            // Custom endpoint
            conf.set("fs.s3a.endpoint", endpoint);
            
            // Path-style access (required for MinIO and some S3-compatible services)
            conf.set("fs.s3a.path.style.access", "true");
            
            // SSL configuration
            conf.set("fs.s3a.connection.ssl.enabled", String.valueOf(sslEnabled));
            
            // Credentials
            conf.set("fs.s3a.access.key", accessKey);
            conf.set("fs.s3a.secret.key", secretAccessKey);
            
            // Allow subclasses to customize configuration
            configureFileSystem(conf);
            
            // Temp directory
            conf.set("hadoop.tmp.dir", System.getProperty("java.io.tmpdir"));
            
            return FileSystem.get(uri, conf);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create S3-compatible filesystem for endpoint: " + endpoint, e);
        }
    }
    
    /**
     * Configure filesystem-specific settings.
     * 
     * <p>Override this method to customize S3A configuration for specific services.
     * Default implementation sets reasonable performance defaults.
     * 
     * <p>Example customization:
     * <pre>
     * {@literal @}Override
     * protected void configureFileSystem(Configuration conf) {
     *     super.configureFileSystem(conf);
     *     // Custom settings for your S3-compatible service
     *     conf.set("fs.s3a.connection.maximum", "100");
     *     conf.set("fs.s3a.connection.timeout", "30000");
     * }
     * </pre>
     * 
     * @param conf The Hadoop configuration to customize
     */
    protected void configureFileSystem(Configuration conf) {
        // Performance tuning defaults
        conf.set("fs.s3a.connection.maximum", "50");
        conf.set("fs.s3a.threads.max", "32");
        conf.set("fs.s3a.multipart.size", "52428800"); // 50MB
        conf.set("fs.s3a.fast.upload.buffer", "bytebuffer");
        
        // Retry configuration
        conf.set("fs.s3a.attempts.maximum", "10");
        conf.set("fs.s3a.retry.limit", "5");
    }
    
    /**
     * Get the configured endpoint URL.
     * 
     * @return The S3-compatible endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * Check if SSL is enabled.
     * 
     * @return true if SSL/TLS is enabled, false otherwise
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }
}
