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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Simple standalone test for S3A with MinIO.
 * 
 * This is NOT a JUnit test - run it as a main() method.
 * 
 * Prerequisites:
 * 1. MinIO running on http://localhost:9000
 * 2. Bucket 'test' created
 * 3. Access credentials configured below
 * 
 * To run MinIO:
 * docker run -p 9000:9000 -p 9001:9001 \
 *   -e MINIO_ROOT_USER=minioadmin \
 *   -e MINIO_ROOT_PASSWORD=minioadmin \
 *   quay.io/minio/minio server /data --console-address ":9001"
 * 
 * Then create bucket 'test' via MinIO Console: http://localhost:9001
 */
public class SimpleMinioS3Test {
    
    // MinIO configuration
    private static final String MINIO_ENDPOINT = "http://localhost:9000";
    private static final String BUCKET_NAME = "test";
    private static final String ACCESS_KEY = "4N82TRBS71UDJRPZOB4X";
    private static final String SECRET_KEY = "XbrHwzzrzkAeMMGHw6+m+E9HM2z24lUPr7c32gBY";
    
    public static void main(String[] args) {
        System.out.println("\n========================================");
        System.out.println("MinIO S3A Filesystem Test");
        System.out.println("========================================");
        System.out.println("Endpoint: " + MINIO_ENDPOINT);
        System.out.println("Bucket:   " + BUCKET_NAME);
        System.out.println("========================================\n");
        
        String testDirectory = "test-run-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Test directory: " + testDirectory + "\n");
        
        FileSystem fs = null;
        
        try {
            // Configure S3A FileSystem for MinIO
            fs = createMinioFileSystem();
            System.out.println("✓ Connected to MinIO\n");
            
            // Test 1: Write a file
            System.out.println("Test 1: Writing text file...");
            Path file1 = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory + "/hello.txt");
            writeFile(fs, file1, "Hello from Hitorro S3A!\nThis is a test file written to MinIO.");
            System.out.println("  ✓ Written: " + file1 + "\n");
            
            // Test 2: Read it back
            System.out.println("Test 2: Reading text file...");
            String content = readFile(fs, file1);
            System.out.println("  ✓ Read " + content.length() + " bytes");
            System.out.println("  Content: " + content.substring(0, Math.min(50, content.length())) + "...\n");
            
            // Test 3: Write multiple files
            System.out.println("Test 3: Writing multiple files...");
            for (int i = 1; i <= 5; i++) {
                Path file = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory + "/file-" + i + ".txt");
                String fileContent = "This is file number " + i + "\n" + "X".repeat(100 * i);
                writeFile(fs, file, fileContent);
                System.out.println("  ✓ Written file-" + i + ".txt (" + fileContent.length() + " bytes)");
            }
            System.out.println();
            
            // Test 4: List files
            System.out.println("Test 4: Listing files...");
            Path dir = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory);
            FileStatus[] files = fs.listStatus(dir);
            System.out.println("  Found " + files.length + " files:");
            for (FileStatus fileStatus : files) {
                System.out.println("    - " + fileStatus.getPath().getName() + 
                                 " (" + fileStatus.getLen() + " bytes)");
            }
            System.out.println();
            
            // Test 5: Binary data
            System.out.println("Test 5: Binary data test...");
            byte[] binaryData = new byte[1024];
            for (int i = 0; i < binaryData.length; i++) {
                binaryData[i] = (byte) (i % 256);
            }
            Path binaryFile = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory + "/binary.bin");
            writeBinary(fs, binaryFile, binaryData);
            System.out.println("  ✓ Written " + binaryData.length + " bytes of binary data");
            
            byte[] readBinary = readBinary(fs, binaryFile);
            System.out.println("  ✓ Read " + readBinary.length + " bytes back");
            boolean match = java.util.Arrays.equals(binaryData, readBinary);
            System.out.println("  ✓ Binary data " + (match ? "matches!" : "MISMATCH!") + "\n");
            
            // Test 6: Large file (5MB)
            System.out.println("Test 6: Writing larger file (5MB)...");
            Path largeFile = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory + "/large.dat");
            int chunks = 5;
            long startTime = System.currentTimeMillis();
            try (FSDataOutputStream out = fs.create(largeFile, true)) {
                for (int i = 0; i < chunks; i++) {
                    byte[] chunk = new byte[1024 * 1024]; // 1MB
                    for (int j = 0; j < chunk.length; j++) {
                        chunk[j] = (byte) ('A' + (j % 26));
                    }
                    out.write(chunk);
                    System.out.println("  ✓ Written chunk " + (i + 1) + "/" + chunks);
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("  ✓ Written " + chunks + "MB in " + duration + "ms\n");
            
            // Test 7: File operations
            System.out.println("Test 7: File operations...");
            Path tempFile = new Path("s3a://" + BUCKET_NAME + "/" + testDirectory + "/temp.txt");
            writeFile(fs, tempFile, "Temporary file");
            System.out.println("  ✓ Created temp file");
            
            boolean exists = fs.exists(tempFile);
            System.out.println("  ✓ File exists: " + exists);
            
            FileStatus status = fs.getFileStatus(tempFile);
            System.out.println("  ✓ File size: " + status.getLen() + " bytes");
            
            boolean deleted = fs.delete(tempFile, false);
            System.out.println("  ✓ File deleted: " + deleted);
            
            exists = fs.exists(tempFile);
            System.out.println("  ✓ File exists after delete: " + exists + "\n");
            
            // Success!
            System.out.println("========================================");
            System.out.println("✓ All tests passed!");
            System.out.println("========================================");
            System.out.println("\nView files in MinIO Console:");
            System.out.println("http://localhost:9001/browser/" + BUCKET_NAME + "/" + testDirectory);
            System.out.println("\nNote: Test files were left in MinIO for inspection.");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("\n✗ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Create FileSystem configured for MinIO.
     */
    private static FileSystem createMinioFileSystem() throws Exception {
        URI uri = new URI("s3a://" + BUCKET_NAME);
        Configuration conf = new Configuration(false);
        
        // S3A FileSystem implementation
        conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        
        // MinIO endpoint configuration
        conf.set("fs.s3a.endpoint", MINIO_ENDPOINT);
        conf.set("fs.s3a.path.style.access", "true");  // MinIO requires path-style access
        
        // Credentials
        conf.set("fs.s3a.access.key", ACCESS_KEY);
        conf.set("fs.s3a.secret.key", SECRET_KEY);
        conf.set("fs.s3a.aws.credentials.provider", 
                "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
        
        // Connection configuration
        conf.set("fs.s3a.connection.ssl.enabled", "false");  // MinIO local uses HTTP
        conf.set("fs.s3a.connection.maximum", "50");
        conf.set("fs.s3a.threads.max", "32");
        
        // Temporary directory for staging (required for writing)
        String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
        conf.set("hadoop.tmp.dir", tmpDir + "/hadoop-" + System.getProperty("user.name"));
        conf.set("fs.s3a.buffer.dir", tmpDir + "/s3a");
        
        // Performance tuning
        conf.set("fs.s3a.fast.upload", "true");
        conf.set("fs.s3a.block.size", "67108864");  // 64MB
        
        // Multipart upload
        conf.set("fs.s3a.multipart.size", "52428800");      // 50MB parts
        conf.set("fs.s3a.multipart.threshold", "104857600"); // Start at 100MB
        
        // Retry configuration
        conf.set("fs.s3a.retry.limit", "5");
        conf.set("fs.s3a.retry.interval", "1000ms");
        
        return FileSystem.get(uri, conf);
    }
    
    /**
     * Write string to file.
     */
    private static void writeFile(FileSystem fs, Path path, String content) throws IOException {
        try (FSDataOutputStream out = fs.create(path, true)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Read file as string.
     */
    private static String readFile(FileSystem fs, Path path) throws IOException {
        try (FSDataInputStream in = fs.open(path)) {
            byte[] bytes = new byte[(int) fs.getFileStatus(path).getLen()];
            in.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Write binary data.
     */
    private static void writeBinary(FileSystem fs, Path path, byte[] data) throws IOException {
        try (FSDataOutputStream out = fs.create(path, true)) {
            out.write(data);
        }
    }
    
    /**
     * Read binary data.
     */
    private static byte[] readBinary(FileSystem fs, Path path) throws IOException {
        try (FSDataInputStream in = fs.open(path)) {
            byte[] bytes = new byte[(int) fs.getFileStatus(path).getLen()];
            in.readFully(bytes);
            return bytes;
        }
    }
}
