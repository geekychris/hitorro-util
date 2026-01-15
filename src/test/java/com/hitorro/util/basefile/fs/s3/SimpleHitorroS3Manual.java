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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.basefile.fs.BaseFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Simple test for Hitorro S3 abstraction with MinIO.
 * 
 * Prerequisites:
 * 1. MinIO running on http://localhost:9000
 * 2. Bucket "hitorro-test" already created
 * 3. MinIO root credentials configured (default: minioadmin/minioadmin)
 * 
 * To run MinIO:
 * docker run -p 9000:9000 -p 9001:9001 \
 *   -e MINIO_ROOT_USER=minioadmin \
 *   -e MINIO_ROOT_PASSWORD=minioadmin \
 *   quay.io/minio/minio server /data --console-address ":9001"
 * 
 * To create bucket:
 * docker exec -it <container_id> mc alias set local http://localhost:9000 minioadmin minioadmin
 * docker exec -it <container_id> mc mb local/hitorro-test
 * 
 * Or use web console at http://localhost:9001
 */
public class SimpleHitorroS3Manual {
    
    // MinIO configuration - CHANGE THESE to match your setup
    private static final String MINIO_ENDPOINT = "http://localhost:9000";
    private static final String BUCKET_NAME = "hitorro-test";
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";
    
    /**
     * Custom S3 filesystem for MinIO.
     */
    static class MinioS3FileSystem extends HTS3FileSystem {
        
        private String endpoint;
        
        public MinioS3FileSystem(String endpoint, String bucketName, String secretAccessKey, String accessKey) {
            super(bucketName, secretAccessKey, accessKey);
            this.endpoint = endpoint;
        }
        
        @Override
        protected FileSystem getFileSystem() {
            try {
                URI uri = new URI(String.format("s3a://%s", getBucketName()));
                Configuration conf = new Configuration(false);
                
                // S3A implementation
                conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
                
                // MinIO endpoint
                conf.set("fs.s3a.endpoint", endpoint);
                conf.set("fs.s3a.path.style.access", "true");
                conf.set("fs.s3a.connection.ssl.enabled", "false");
                
                // Credentials
                conf.set("fs.s3a.access.key", accessKey);
                conf.set("fs.s3a.secret.key", secretAccessKey);
                
                // Performance tuning
                conf.set("fs.s3a.connection.maximum", "50");
                conf.set("fs.s3a.threads.max", "32");
                conf.set("fs.s3a.multipart.size", "52428800"); // 50MB
                conf.set("fs.s3a.fast.upload.buffer", "bytebuffer");
                
                // Temp directory
                conf.set("hadoop.tmp.dir", System.getProperty("java.io.tmpdir"));
                
                return FileSystem.get(uri, conf);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create S3 filesystem", e);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("\n==============================================================");
        System.out.println("Hitorro S3 Abstraction Test with MinIO");
        System.out.println("==============================================================\n");
        
        try {
            // Initialize Hitorro environment
            initializeHitorroEnvironment();
            
            // Create unique test directory
            String testDirectory = "test-run-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("Test directory: " + testDirectory + "\n");
            
            // Create filesystem
            MinioS3FileSystem s3 = new MinioS3FileSystem(
                MINIO_ENDPOINT, 
                BUCKET_NAME, 
                SECRET_KEY, 
                ACCESS_KEY
            );
            
            System.out.println("✓ Connected to MinIO at " + MINIO_ENDPOINT);
            System.out.println("✓ Using bucket: " + BUCKET_NAME);
            System.out.println();
            
            // Run tests
            int passed = 0;
            int total = 0;
            
            passed += runTest(++total, "Write text file", () -> testWriteTextFile(s3, testDirectory));
            passed += runTest(++total, "Check file exists and metadata", () -> testFileMetadata(s3, testDirectory));
            passed += runTest(++total, "Read file back", () -> testReadFile(s3, testDirectory));
            passed += runTest(++total, "Write multiple files", () -> testMultipleFiles(s3, testDirectory));
            passed += runTest(++total, "Binary data", () -> testBinaryData(s3, testDirectory));
            passed += runTest(++total, "Copy file", () -> testCopyFile(s3, testDirectory));
            passed += runTest(++total, "Large file streaming (1MB)", () -> testLargeFile(s3, testDirectory));
            passed += runTest(++total, "Delete operations", () -> testDelete(s3, testDirectory));
            
            // Summary
            System.out.println("\n==============================================================");
            System.out.println("Test Summary: " + passed + "/" + total + " passed");
            System.out.println("==============================================================");
            
            if (passed == total) {
                System.out.println("\n✓ ALL TESTS PASSED! S3A integration working perfectly.");
                System.out.println("\nYou can view the test files in MinIO console:");
                System.out.println("  http://localhost:9001/browser/" + BUCKET_NAME + "/" + testDirectory);
            } else {
                System.out.println("\n✗ Some tests failed. Check output above.");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("\n✗ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Initialize minimal Hitorro environment for testing.
     */
    private static void initializeHitorroEnvironment() {
        try {
            System.out.println("Initializing Hitorro environment...");
            
            // Create JVS properties
            JVS props = new JVS();
            
            // Set required system properties
            Map<String, String> systemProps = new HashMap<>();
            systemProps.put("HT_BIN", System.getProperty("user.home") + "/hitorro");
            systemProps.put("HT_HOME", System.getProperty("user.home") + "/hthome");
            systemProps.put("ht_data", System.getProperty("user.home") + "/hitorro/data");
            props.addMap(systemProps);
            
            // Set default properties
            JVSProperties.setDefaultProperties(props, false);
            
            System.out.println("✓ Hitorro environment initialized\n");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Hitorro environment", e);
        }
    }
    
    private static int runTest(int number, String name, TestRunnable test) {
        try {
            System.out.printf("Test %d: %s... ", number, name);
            test.run();
            System.out.println("✓ PASSED");
            return 1;
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    interface TestRunnable {
        void run() throws Exception;
    }
    
    // TEST 1: Write text file
    private static void testWriteTextFile(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile file = s3.getFile(testDir + "/test.txt");
        try (OutputStream os = file.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write("Hello from Hitorro S3!");
        }
    }
    
    // TEST 2: Check file metadata
    private static void testFileMetadata(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile file = s3.getFile(testDir + "/test.txt");
        if (!file.exists()) {
            throw new AssertionError("File should exist");
        }
        if (file.length() <= 0) {
            throw new AssertionError("File should have content");
        }
    }
    
    // TEST 3: Read file back
    private static void testReadFile(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile file = s3.getFile(testDir + "/test.txt");
        try (InputStream is = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {
            String content = br.readLine();
            if (!"Hello from Hitorro S3!".equals(content)) {
                throw new AssertionError("Content mismatch: " + content);
            }
        }
    }
    
    // TEST 4: Write multiple files
    private static void testMultipleFiles(MinioS3FileSystem s3, String testDir) throws Exception {
        for (int i = 1; i <= 3; i++) {
            BaseFile file = s3.getFile(testDir + "/file" + i + ".txt");
            try (OutputStream os = file.getOutputStream()) {
                os.write(("File " + i).getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    
    // TEST 5: Binary data
    private static void testBinaryData(MinioS3FileSystem s3, String testDir) throws Exception {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        
        BaseFile file = s3.getFile(testDir + "/binary.dat");
        try (OutputStream os = file.getOutputStream()) {
            os.write(data);
        }
        
        byte[] readData = new byte[256];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(readData);
            if (read != 256) {
                throw new AssertionError("Expected 256 bytes, got " + read);
            }
            if (!Arrays.equals(data, readData)) {
                throw new AssertionError("Binary data mismatch");
            }
        }
    }
    
    // TEST 6: Copy file
    private static void testCopyFile(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile src = s3.getFile(testDir + "/test.txt");
        BaseFile dst = s3.getFile(testDir + "/test-copy.txt");
        
        try (InputStream is = src.getInputStream();
             OutputStream os = dst.getOutputStream()) {
            is.transferTo(os);
        }
        
        if (!dst.exists()) {
            throw new AssertionError("Copied file should exist");
        }
    }
    
    // TEST 7: Large file streaming
    private static void testLargeFile(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile file = s3.getFile(testDir + "/large.dat");
        
        // Write 1MB file
        int size = 1024 * 1024;
        try (OutputStream os = file.getOutputStream()) {
            byte[] buffer = new byte[8192];
            Arrays.fill(buffer, (byte) 'A');
            for (int i = 0; i < size / buffer.length; i++) {
                os.write(buffer);
            }
        }
        
        if (file.length() != size) {
            throw new AssertionError("File size mismatch: " + file.length() + " != " + size);
        }
    }
    
    // TEST 8: Delete operations
    private static void testDelete(MinioS3FileSystem s3, String testDir) throws Exception {
        BaseFile file = s3.getFile(testDir + "/to-delete.txt");
        
        // Create file
        try (OutputStream os = file.getOutputStream()) {
            os.write("Delete me".getBytes(StandardCharsets.UTF_8));
        }
        
        if (!file.exists()) {
            throw new AssertionError("File should exist before delete");
        }
        
        // Delete
        if (!file.delete()) {
            throw new AssertionError("Delete should return true");
        }
        
        if (file.exists()) {
            throw new AssertionError("File should not exist after delete");
        }
    }
}
