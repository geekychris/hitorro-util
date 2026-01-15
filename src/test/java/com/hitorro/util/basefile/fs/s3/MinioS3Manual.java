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

import com.hitorro.util.basefile.fs.BaseFile;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test S3A filesystem implementation with MinIO (local S3-compatible object storage).
 * 
 * Prerequisites:
 * 1. MinIO running on http://localhost:9000
 * 2. Bucket 'test' created
 * 3. Access credentials configured
 * 
 * To run MinIO locally:
 * docker run -p 9000:9000 -p 9001:9001 \
 *   -e MINIO_ROOT_USER=minioadmin \
 *   -e MINIO_ROOT_PASSWORD=minioadmin \
 *   quay.io/minio/minio server /data --console-address ":9001"
 * 
 * Create bucket 'test' via MinIO Console: http://localhost:9001
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MinioS3Manual {
    
    // MinIO configuration (local S3-compatible storage)
    private static final String MINIO_ENDPOINT = "http://localhost:9000";
    private static final String BUCKET_NAME = "test";
    private static final String ACCESS_KEY = "4N82TRBS71UDJRPZOB4X";
    private static final String SECRET_KEY = "XbrHwzzrzkAeMMGHw6+m+E9HM2z24lUPr7c32gBY";
    
    private static MinioS3FileSystem fileSystem;
    private static String testDirectory;
    
    /**
     * Custom S3 filesystem implementation for MinIO.
     * Extends HTS3FileSystem to configure MinIO-specific endpoint.
     */
    static class MinioS3FileSystem extends HTS3FileSystem {
        
        private String endpoint;
        
        public MinioS3FileSystem(String endpoint, String bucketName, String secretAccessKey, String accessKey) {
            super(bucketName, secretAccessKey, accessKey);
            this.endpoint = endpoint;
        }
        
        @Override
        protected org.apache.hadoop.fs.FileSystem getFileSystem() {
            org.apache.hadoop.fs.FileSystem ret = null;
            
            try {
                java.net.URI uri = new java.net.URI(String.format("s3a://%s", getBucketName()));
                org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration(false);
                
                // S3A FileSystem implementation
                conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
                
                // MinIO endpoint configuration (key difference from AWS S3)
                conf.set("fs.s3a.endpoint", endpoint);
                conf.set("fs.s3a.path.style.access", "true");  // MinIO requires path-style access
                
                // Credentials
                conf.set("fs.s3a.access.key", accessKey);
                conf.set("fs.s3a.secret.key", secretAccessKey);
                conf.set("fs.s3a.aws.credentials.provider", 
                        "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
                
                // Connection configuration
                conf.set("fs.s3a.connection.ssl.enabled", "false");  // MinIO local uses HTTP
                conf.set("fs.s3a.connection.maximum", "50");
                conf.set("fs.s3a.threads.max", "32");
                
                // Performance tuning
                conf.set("fs.s3a.fast.upload", "true");
                conf.set("fs.s3a.block.size", "67108864");  // 64MB
                
                // Multipart upload
                conf.set("fs.s3a.multipart.size", "52428800");      // 50MB parts
                conf.set("fs.s3a.multipart.threshold", "104857600"); // Start at 100MB
                
                // Retry configuration
                conf.set("fs.s3a.retry.limit", "5");
                conf.set("fs.s3a.retry.interval", "1000ms");
                
                ret = org.apache.hadoop.fs.FileSystem.get(uri, conf);
                triedAndFailed = false;
                
                System.out.println("✓ Connected to MinIO at " + endpoint + " bucket: " + getBucketName());
                return ret;
                
            } catch (Exception e) {
                System.err.println("✗ Failed to connect to MinIO: " + e.getMessage());
                e.printStackTrace();
                triedAndFailed = true;
            }
            
            return ret;
        }
    }
    
    @BeforeAll
    static void setup() {
        System.out.println("\n========================================");
        System.out.println("MinIO S3A Filesystem Test");
        System.out.println("========================================");
        System.out.println("Endpoint: " + MINIO_ENDPOINT);
        System.out.println("Bucket:   " + BUCKET_NAME);
        System.out.println("========================================\n");
        
        // Create unique test directory for this run
        testDirectory = "test-run-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Test directory: " + testDirectory + "\n");
        
        // Create filesystem
        fileSystem = new MinioS3FileSystem(MINIO_ENDPOINT, BUCKET_NAME, SECRET_KEY, ACCESS_KEY);
        
        // Verify connection
        assertTrue(fileSystem.isFileSystemAvailable(), 
                  "MinIO filesystem should be available at " + MINIO_ENDPOINT);
        
        System.out.println("✓ MinIO connection verified\n");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Write a simple text file")
    void testWriteTextFile() throws IOException {
        System.out.println("Test 1: Writing text file...");
        
        // Get file handle
        BaseFile file = fileSystem.getFile(testDirectory + "/hello.txt");
        assertNotNull(file, "File handle should not be null");
        
        // Write content
        String content = "Hello from Hitorro S3A!\nThis is a test file written to MinIO.";
        try (OutputStream os = file.getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
        
        System.out.println("  ✓ Written " + content.length() + " bytes to: " + testDirectory + "/hello.txt");
        
        // Verify file exists
        assertTrue(file.exists(), "File should exist after write");
        System.out.println("  ✓ File exists confirmed\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Read the text file back")
    void testReadTextFile() throws IOException {
        System.out.println("Test 2: Reading text file...");
        
        // Get file handle
        BaseFile file = fileSystem.getFile(testDirectory + "/hello.txt");
        assertTrue(file.exists(), "File should exist");
        
        // Read content
        String content;
        try (InputStream is = file.getInputStream()) {
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        
        System.out.println("  ✓ Read " + content.length() + " bytes");
        System.out.println("  Content: " + content.substring(0, Math.min(50, content.length())) + "...");
        
        // Verify content
        assertTrue(content.startsWith("Hello from Hitorro S3A!"), "Content should match");
        System.out.println("  ✓ Content verified\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Write multiple files")
    void testWriteMultipleFiles() throws IOException {
        System.out.println("Test 3: Writing multiple files...");
        
        int fileCount = 5;
        for (int i = 1; i <= fileCount; i++) {
            BaseFile file = fileSystem.getFile(testDirectory + "/file-" + i + ".txt");
            String content = "This is file number " + i + "\n" +
                           "Created at: " + System.currentTimeMillis() + "\n" +
                           "Content: " + "X".repeat(100 * i); // Variable size
            
            try (OutputStream os = file.getOutputStream()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
            
            System.out.println("  ✓ Written file-" + i + ".txt (" + content.length() + " bytes)");
        }
        
        System.out.println("  ✓ All " + fileCount + " files written\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. List files in directory")
    void testListFiles() throws IOException {
        System.out.println("Test 4: Listing files...");
        
        BaseFile dir = fileSystem.getFile(testDirectory);
        BaseFile[] files = dir.listFiles();
        
        assertNotNull(files, "File list should not be null");
        assertTrue(files.length >= 6, "Should have at least 6 files (1 hello.txt + 5 file-*.txt)");
        
        System.out.println("  Found " + files.length + " files:");
        for (BaseFile file : files) {
            long size = file.length();
            System.out.println("    - " + file.getName() + " (" + size + " bytes)");
        }
        System.out.println();
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Write and read binary data")
    void testBinaryData() throws IOException {
        System.out.println("Test 5: Binary data test...");
        
        // Create binary data
        byte[] originalData = new byte[1024];
        for (int i = 0; i < originalData.length; i++) {
            originalData[i] = (byte) (i % 256);
        }
        
        // Write binary file
        BaseFile file = fileSystem.getFile(testDirectory + "/binary-data.bin");
        try (OutputStream os = file.getOutputStream()) {
            os.write(originalData);
        }
        System.out.println("  ✓ Written " + originalData.length + " bytes of binary data");
        
        // Read back
        byte[] readData;
        try (InputStream is = file.getInputStream()) {
            readData = is.readAllBytes();
        }
        System.out.println("  ✓ Read " + readData.length + " bytes back");
        
        // Verify
        assertArrayEquals(originalData, readData, "Binary data should match exactly");
        System.out.println("  ✓ Binary data verified (all bytes match)\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Write larger file (multipart test)")
    void testLargerFile() throws IOException {
        System.out.println("Test 6: Writing larger file...");
        
        BaseFile file = fileSystem.getFile(testDirectory + "/large-file.txt");
        
        // Write 5MB of data
        int chunkSize = 1024 * 1024; // 1MB
        int chunks = 5;
        
        long startTime = System.currentTimeMillis();
        try (OutputStream os = file.getOutputStream()) {
            for (int i = 0; i < chunks; i++) {
                byte[] chunk = new byte[chunkSize];
                // Fill with pattern
                for (int j = 0; j < chunkSize; j++) {
                    chunk[j] = (byte) ('A' + (j % 26));
                }
                os.write(chunk);
                System.out.println("  ✓ Written chunk " + (i + 1) + "/" + chunks + " (1MB)");
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("  ✓ Written " + (chunks) + "MB in " + duration + "ms");
        
        // Verify size
        long fileSize = file.length();
        assertEquals(chunkSize * chunks, fileSize, "File size should match");
        System.out.println("  ✓ File size verified: " + fileSize + " bytes\n");
    }
    
    @Test
    @Order(7)
    @DisplayName("7. Test file operations (exists, delete)")
    void testFileOperations() throws IOException {
        System.out.println("Test 7: File operations test...");
        
        // Create a test file
        BaseFile file = fileSystem.getFile(testDirectory + "/temp-file.txt");
        try (OutputStream os = file.getOutputStream()) {
            os.write("Temporary file".getBytes());
        }
        
        // Check exists
        assertTrue(file.exists(), "File should exist after creation");
        System.out.println("  ✓ File exists: " + file.getRelativePath());
        
        // Get length
        long length = file.length();
        assertTrue(length > 0, "File should have content");
        System.out.println("  ✓ File length: " + length + " bytes");
        
        // Delete file
        boolean deleted = file.delete();
        assertTrue(deleted, "Delete should succeed");
        System.out.println("  ✓ File deleted");
        
        // Verify doesn't exist
        assertFalse(file.exists(), "File should not exist after deletion");
        System.out.println("  ✓ Deletion verified\n");
    }
    
    @Test
    @Order(8)
    @DisplayName("8. Test subdirectories")
    void testSubdirectories() throws IOException {
        System.out.println("Test 8: Subdirectory test...");
        
        // Create nested structure
        String[] paths = {
            testDirectory + "/subdir1/file1.txt",
            testDirectory + "/subdir1/file2.txt",
            testDirectory + "/subdir2/file3.txt",
            testDirectory + "/subdir2/nested/file4.txt"
        };
        
        for (String path : paths) {
            BaseFile file = fileSystem.getFile(path);
            try (OutputStream os = file.getOutputStream()) {
                os.write(("Content of " + path).getBytes());
            }
            System.out.println("  ✓ Created: " + path);
        }
        
        // List subdir1
        BaseFile subdir1 = fileSystem.getFile(testDirectory + "/subdir1");
        BaseFile[] files = subdir1.listFiles();
        assertNotNull(files, "Directory listing should not be null");
        System.out.println("  ✓ Found " + files.length + " files in subdir1");
        
        System.out.println();
    }
    
    @Test
    @Order(9)
    @DisplayName("9. Performance benchmark")
    void testPerformanceBenchmark() throws IOException {
        System.out.println("Test 9: Performance benchmark...");
        
        int iterations = 10;
        int fileSize = 10 * 1024; // 10KB per file
        
        // Write benchmark
        long writeStart = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            BaseFile file = fileSystem.getFile(testDirectory + "/perf/write-" + i + ".txt");
            byte[] data = new byte[fileSize];
            try (OutputStream os = file.getOutputStream()) {
                os.write(data);
            }
        }
        long writeDuration = System.currentTimeMillis() - writeStart;
        double writeMBps = (iterations * fileSize / 1024.0 / 1024.0) / (writeDuration / 1000.0);
        
        System.out.println("  Write: " + iterations + " files x " + fileSize + " bytes in " + 
                         writeDuration + "ms (" + String.format("%.2f", writeMBps) + " MB/s)");
        
        // Read benchmark
        long readStart = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            BaseFile file = fileSystem.getFile(testDirectory + "/perf/write-" + i + ".txt");
            try (InputStream is = file.getInputStream()) {
                is.readAllBytes();
            }
        }
        long readDuration = System.currentTimeMillis() - readStart;
        double readMBps = (iterations * fileSize / 1024.0 / 1024.0) / (readDuration / 1000.0);
        
        System.out.println("  Read:  " + iterations + " files x " + fileSize + " bytes in " + 
                         readDuration + "ms (" + String.format("%.2f", readMBps) + " MB/s)");
        System.out.println();
    }
    
    @AfterAll
    static void cleanup() {
        System.out.println("========================================");
        System.out.println("Test Cleanup");
        System.out.println("========================================");
        
        if (fileSystem != null && fileSystem.isFileSystemAvailable()) {
            try {
                // Optional: Clean up test directory
                // Uncomment to delete test files after run
                /*
                BaseFile testDir = fileSystem.getFile(testDirectory);
                if (testDir.exists()) {
                    // Delete all files recursively
                    deleteRecursive(testDir);
                    System.out.println("✓ Cleaned up test directory: " + testDirectory);
                }
                */
                System.out.println("Note: Test files left in MinIO bucket 'test' at: " + testDirectory);
                System.out.println("      View them at: http://localhost:9001/browser/test/" + testDirectory);
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
        
        System.out.println("========================================");
        System.out.println("All tests complete!");
        System.out.println("========================================\n");
    }
    
    /**
     * Helper method to delete directory recursively.
     */
    private static void deleteRecursive(BaseFile file) throws IOException {
        // Check if it's a directory by trying to list files
        BaseFile[] children = file.listFiles();
        if (children != null && children.length > 0) {
            for (BaseFile child : children) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }
}
