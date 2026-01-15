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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprehensive test for Hitorro S3 abstraction with MinIO.
 * 
 * Demonstrates TWO authentication methods:
 * 1. Root username/password credentials
 * 2. App tokens (access keys) created via mc commands
 * 
 * Prerequisites:
 * 1. MinIO running on http://localhost:9000
 * 2. Docker container with mc (MinIO Client) available
 * 
 * Setup Commands:
 * <pre>
 * # Start MinIO
 * docker run -p 9000:9000 -p 9001:9001 \
 *   -e MINIO_ROOT_USER=admin \
 *   -e MINIO_ROOT_PASSWORD=mypassword \
 *   quay.io/minio/minio server /data --console-address ":9001"
 * 
 * # Configure mc alias (run once)
 * docker exec -it <container_id> mc alias set mylocal http://localhost:9000 admin mypassword
 * 
 * # Create bucket (run once)
 * docker exec -it <container_id> mc mb mylocal/test
 * 
 * # Create app token (run once or automated by this test)
 * docker exec -it <container_id> mc admin accesskey create mylocal/admin \
 *   --name hitorro-test-app \
 *   --description "Hitorro Test App Key" \
 *   --expiry-duration 2400h
 * </pre>
 */
public class HitorroS3AbstractionTest {
    
    // MinIO configuration
    private static final String MINIO_ENDPOINT = "http://localhost:9000";
    private static final String BUCKET_NAME = "test";
    private static final String MC_ALIAS = "mylocal";
    
    // Root credentials (for setup and Method 1)
    private static final String MINIO_ROOT_USER = 
        System.getenv().getOrDefault("MINIO_ROOT_USER", "admin");
    private static final String MINIO_ROOT_PASSWORD = 
        System.getenv().getOrDefault("MINIO_ROOT_PASSWORD", "<PUT_PASSWORD_HERE>");
    
    // App token credentials (for Method 2, created dynamically)
    private static String appAccessKey;
    private static String appSecretKey;
    
    // Docker container ID (auto-detected)
    private static String minioContainerId;
    
    public static void main(String[] args) {
        System.out.println("\n==============================================================");
        System.out.println("Hitorro S3 Abstraction Test - Dual Authentication Demo");
        System.out.println("==============================================================\n");
        
        try {
            // Initialize Hitorro environment
            System.out.println("Step 1: Initializing Hitorro environment...");
            initializeHitorroEnvironment();
            System.out.println("  ✓ Hitorro environment initialized\n");
            
            // Detect MinIO container
            System.out.println("Step 2: Detecting MinIO container...");
            minioContainerId = detectMinIOContainer();
            System.out.println("  ✓ MinIO container: " + minioContainerId + "\n");
            
            // Setup mc alias
            System.out.println("Step 3: Configuring mc alias...");
            setupMcAlias();
            System.out.println("  ✓ mc alias configured\n");
            
            // Verify bucket exists
            System.out.println("Step 4: Verifying bucket '" + BUCKET_NAME + "'...");
            verifyBucket();
            System.out.println("  ✓ Bucket verified\n");
            
            // Create app token using mc
            System.out.println("Step 5: Creating app token via mc...");
            createAppToken();
            System.out.println("  ✓ App Access Key: " + appAccessKey);
            System.out.println("  ✓ App Secret Key: " + appSecretKey.substring(0, 10) + "...\n");
            
            // Test directory
            String testDirectory = "test-run-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("Test directory: " + testDirectory);
            System.out.println("==============================================================\n");
            
            // METHOD 1: Test with root credentials
            System.out.println("METHOD 1: Testing with ROOT CREDENTIALS");
            System.out.println("----------------------------------------");
            System.out.println("Using: " + MINIO_ROOT_USER + " / " + MINIO_ROOT_PASSWORD);
            runS3Tests(MINIO_ROOT_USER, MINIO_ROOT_PASSWORD, testDirectory + "/root-auth");
            System.out.println("✓ Root credential authentication SUCCESS\n");
            
            // METHOD 2: Test with app token
            System.out.println("METHOD 2: Testing with APP TOKEN");
            System.out.println("----------------------------------------");
            System.out.println("Using: " + appAccessKey + " / " + appSecretKey.substring(0, 10) + "...");
            runS3Tests(appAccessKey, appSecretKey, testDirectory + "/app-token-auth");
            System.out.println("✓ App token authentication SUCCESS\n");
            
            // Success!
            System.out.println("==============================================================");
            System.out.println("✓ ALL TESTS PASSED WITH BOTH AUTHENTICATION METHODS!");
            System.out.println("==============================================================");
            System.out.println("\nAuthentication Methods Demonstrated:");
            System.out.println("  1. Root username/password: " + MINIO_ROOT_USER + " / " + MINIO_ROOT_PASSWORD);
            System.out.println("  2. App token (access key): " + appAccessKey);
            System.out.println("\nView files in MinIO Console:");
            System.out.println("  http://localhost:9001/browser/" + BUCKET_NAME + "/" + testDirectory);
            System.out.println("\nClean up app token:");
            System.out.println("  docker exec -it " + minioContainerId + " mc admin accesskey rm " + MC_ALIAS + " " + appAccessKey);
            System.out.println("==============================================================\n");
            
        } catch (Exception e) {
            System.err.println("\n✗ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Initialize minimal Hitorro environment for testing.
     */
    private static void initializeHitorroEnvironment() {
        try {
            JVS props = new JVS();
            Map<String, String> systemProps = new HashMap<>();
            systemProps.put("HT_BIN", System.getProperty("user.home") + "/hitorro");
            systemProps.put("HT_HOME", System.getProperty("user.home") + "/hthome");
            systemProps.put("ht_data", System.getProperty("user.home") + "/hitorro/data");
            props.addMap(systemProps);
            JVSProperties.setDefaultProperties(props, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Hitorro environment", e);
        }
    }
    
    /**
     * Detect MinIO docker container ID.
     */
    private static String detectMinIOContainer() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"docker", "ps", "-q", "-f", "ancestor=quay.io/minio/minio"});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String containerId = reader.readLine();
        process.waitFor();
        
        if (containerId == null || containerId.trim().isEmpty()) {
            throw new RuntimeException("MinIO container not found. Is MinIO running?");
        }
        
        return containerId.trim();
    }
    
    /**
     * Setup mc alias for MinIO.
     */
    private static void setupMcAlias() throws IOException, InterruptedException {
        String[] cmd = {
            "docker", "exec", minioContainerId,
            "mc", "alias", "set", MC_ALIAS, "http://localhost:9000", 
            MINIO_ROOT_USER, MINIO_ROOT_PASSWORD
        };
        
        Process process = Runtime.getRuntime().exec(cmd);
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error = errorReader.lines().reduce("", (a, b) -> a + b);
            throw new RuntimeException("Failed to setup mc alias: " + error);
        }
    }
    
    /**
     * Verify bucket exists, create if needed.
     */
    private static void verifyBucket() throws IOException, InterruptedException {
        // Check if bucket exists
        String[] listCmd = {"docker", "exec", minioContainerId, "mc", "ls", MC_ALIAS + "/" + BUCKET_NAME};
        Process listProcess = Runtime.getRuntime().exec(listCmd);
        int exitCode = listProcess.waitFor();
        
        if (exitCode != 0) {
            // Bucket doesn't exist, create it
            System.out.println("  → Bucket doesn't exist, creating...");
            String[] createCmd = {"docker", "exec", minioContainerId, "mc", "mb", MC_ALIAS + "/" + BUCKET_NAME};
            Process createProcess = Runtime.getRuntime().exec(createCmd);
            int createExitCode = createProcess.waitFor();
            
            if (createExitCode != 0) {
                throw new RuntimeException("Failed to create bucket");
            }
            System.out.println("  → Bucket created");
        } else {
            System.out.println("  → Bucket exists");
        }
    }
    
    /**
     * Create app token using mc admin accesskey create command.
     */
    private static void createAppToken() throws IOException, InterruptedException {
        String appName = "hitorro-test-" + UUID.randomUUID().toString().substring(0, 8);
        
        String[] cmd = {
            "docker", "exec", minioContainerId,
            "mc", "admin", "accesskey", "create", MC_ALIAS + "/" + MINIO_ROOT_USER,
            "--name", appName,
            "--description", "Hitorro Test App Token",
            "--expiry-duration", "24h"
        };
        
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        // Parse output to extract access key and secret key
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("  → mc: " + line);
            
            // Look for "Access Key: ..." pattern
            if (line.contains("Access Key:") || line.contains("AccessKey:")) {
                Pattern pattern = Pattern.compile("Access Key:\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    appAccessKey = matcher.group(1);
                }
            }
            
            // Look for "Secret Key: ..." pattern
            if (line.contains("Secret Key:") || line.contains("SecretKey:")) {
                Pattern pattern = Pattern.compile("Secret Key:\\s*([A-Za-z0-9+/=]+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    appSecretKey = matcher.group(1);
                }
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0 || appAccessKey == null || appSecretKey == null) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error = errorReader.lines().reduce("", (a, b) -> a + "\n" + b);
            throw new RuntimeException("Failed to create app token: " + error);
        }
    }
    
    /**
     * Run S3 tests with given credentials.
     */
    private static void runS3Tests(String accessKey, String secretKey, String testDirectory) throws IOException {
        // Create filesystem using S3CompatibleFileSystem
        S3CompatibleFileSystem s3 = new S3CompatibleFileSystem(
            MINIO_ENDPOINT,  // endpoint
            BUCKET_NAME,     // bucket
            accessKey,       // access key
            secretKey,       // secret key
            false            // SSL disabled for local MinIO
        );
        
        // Test 1: Write text file
        System.out.print("  Test 1: Write text file... ");
        BaseFile file1 = s3.getFile(testDirectory + "/test.txt");
        try (OutputStream os = file1.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write("Hello from Hitorro S3! Auth: " + accessKey);
        }
        System.out.println("✓");
        
        // Test 2: Check exists and metadata
        System.out.print("  Test 2: Check file exists and metadata... ");
        if (!file1.exists()) throw new AssertionError("File should exist");
        if (file1.length() <= 0) throw new AssertionError("File should have content");
        System.out.println("✓");
        
        // Test 3: Read file back
        System.out.print("  Test 3: Read file back... ");
        try (InputStream is = file1.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {
            String content = br.readLine();
            if (!content.contains("Hello from Hitorro S3!")) {
                throw new AssertionError("Content mismatch: " + content);
            }
        }
        System.out.println("✓");
        
        // Test 4: Write multiple files
        System.out.print("  Test 4: Write multiple files... ");
        for (int i = 1; i <= 3; i++) {
            BaseFile file = s3.getFile(testDirectory + "/file" + i + ".txt");
            try (OutputStream os = file.getOutputStream()) {
                os.write(("File " + i + " - Auth: " + accessKey).getBytes(StandardCharsets.UTF_8));
            }
        }
        System.out.println("✓");
        
        // Test 5: Binary data
        System.out.print("  Test 5: Binary data... ");
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        BaseFile binFile = s3.getFile(testDirectory + "/binary.dat");
        try (OutputStream os = binFile.getOutputStream()) {
            os.write(data);
        }
        byte[] readData = new byte[256];
        try (InputStream is = binFile.getInputStream()) {
            int read = is.read(readData);
            if (read != 256 || !Arrays.equals(data, readData)) {
                throw new AssertionError("Binary data mismatch");
            }
        }
        System.out.println("✓");
        
        // Test 6: Large file (500KB)
        System.out.print("  Test 6: Large file (500KB)... ");
        BaseFile largeFile = s3.getFile(testDirectory + "/large.dat");
        int size = 500 * 1024; // 512000 bytes
        int bytesWritten = 0;
        try (OutputStream os = largeFile.getOutputStream()) {
            byte[] buffer = new byte[8192];
            Arrays.fill(buffer, (byte) 'A');
            
            // Write full buffers
            int fullBuffers = size / buffer.length;
            for (int i = 0; i < fullBuffers; i++) {
                os.write(buffer);
                bytesWritten += buffer.length;
            }
            
            // Write remaining bytes
            int remainder = size % buffer.length;
            if (remainder > 0) {
                os.write(buffer, 0, remainder);
                bytesWritten += remainder;
            }
            
            // Explicitly flush (though try-with-resources will also close/flush)
            os.flush();
        } // try-with-resources automatically calls os.close() here
        
        // Get fresh file handle to check actual size
        BaseFile verifyFile = s3.getFile(testDirectory + "/large.dat");
        long actualSize = verifyFile.length();
        
        if (actualSize != size) {
            throw new AssertionError("File size mismatch: expected " + size + ", got " + actualSize + ", wrote " + bytesWritten);
        }
        System.out.println("✓");
    }
}
