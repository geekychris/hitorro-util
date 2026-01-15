# S3/MinIO Manual Tests Guide

## Overview

The S3/MinIO test files have been renamed to **`*Manual.java`** so they are **NOT run automatically** by Maven. This prevents test failures when MinIO is not running.

## Manual Test Files

All located in: `hitorro-util/src/test/java/com/hitorro/util/basefile/fs/s3/`

1. **HitorroS3AbstractionManual.java** - Comprehensive Hitorro BaseFile API with MinIO
2. **SimpleHitorroS3Manual.java** - Simple Hitorro abstraction test
3. **SimpleMinioS3Manual.java** - Simple low-level Hadoop S3A test
4. **MinioS3Manual.java** - Original MinIO test

## Why Renamed?

**Before**: Files ended in `Test.java`
- Maven Surefire automatically runs them
- Tests fail if MinIO not running
- CI/CD builds break

**After**: Files end in `Manual.java`
- Maven Surefire ignores them
- Only run when explicitly requested
- Clean builds without MinIO

## How to Run Manually

### Option 1: Maven exec:java (Recommended)

```bash
cd hitorro-util

# Run the comprehensive test
mvn exec:java \
  -Dexec.mainClass="com.hitorro.util.basefile.fs.s3.HitorroS3AbstractionManual" \
  -Dexec.classpathScope=test

# Run simple Hitorro test
mvn exec:java \
  -Dexec.mainClass="com.hitorro.util.basefile.fs.s3.SimpleHitorroS3Manual" \
  -Dexec.classpathScope=test

# Run simple MinIO test
mvn exec:java \
  -Dexec.mainClass="com.hitorro.util.basefile.fs.s3.SimpleMinioS3Manual" \
  -Dexec.classpathScope=test
```

### Option 2: IntelliJ IDEA

1. Open the `*Manual.java` file in IntelliJ
2. Right-click on the `public static void main()` method
3. Select "Run 'ClassName.main()'"

### Option 3: Command Line Java

```bash
cd hitorro-util
mvn test-compile

java -cp target/test-classes:target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
  com.hitorro.util.basefile.fs.s3.HitorroS3AbstractionManual
```

## Prerequisites

All S3 tests require **MinIO running locally**:

```bash
# Start MinIO
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  quay.io/minio/minio server /data --console-address ":9001"

# Access MinIO Console
# Browser: http://localhost:9001
# Username: minioadmin
# Password: minioadmin

# Create bucket 'test' via web console
```

## Test Descriptions

### HitorroS3AbstractionManual (Most Comprehensive)

**Tests**: Hitorro's BaseFile abstraction with S3A
- Two authentication methods (root credentials + app tokens)
- Automatic mc command integration
- 6 comprehensive tests per method
- Full workflow demonstration

**Run time**: ~10 seconds
**Data written**: ~10MB

### SimpleHitorroS3Manual

**Tests**: Basic Hitorro BaseFile operations
- Write/read text files
- Binary data
- Large files
- Metadata operations

**Run time**: ~5 seconds
**Data written**: ~3MB

### SimpleMinioS3Manual

**Tests**: Low-level Hadoop FileSystem API
- Direct S3A operations
- FSDataInputStream/FSDataOutputStream
- Performance benchmarking

**Run time**: ~5 seconds
**Data written**: ~10MB

## Verification

After running, verify results:

```bash
# View files in MinIO Console
# http://localhost:9001/browser/test/

# Or via mc command
docker exec -it <container_id> mc ls mylocal/test/
```

## Cleanup

```bash
# Stop MinIO
docker stop <container_id>

# Remove container
docker rm <container_id>

# Remove data (optional)
docker volume prune
```

## Status

✅ **S3 tests disabled from automatic runs**  
✅ **Can still run manually when needed**  
✅ **CI/CD builds won't fail**  
✅ **Full S3A functionality tested when MinIO available**  

The tests remain available for manual validation but won't interfere with automated builds! 🎯
