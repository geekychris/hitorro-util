# Transformation Testing Guide

## Overview

This guide explains how to test the Hitorro transformation system using the new test endpoints. These endpoints create documents, add content, and trigger transformations - perfect for debugging and single-stepping through the transformation pipeline.

## New Test Endpoint

**Controller**: `TransformationTestController`  
**Base Path**: `/api/test/transformations`  
**Location**: `hitorro-example-springboot/src/main/java/com/hitorro/example/controller/TransformationTestController.java`

## Available Test Endpoints

### 1. List All Available Transformations

See what transformations are available for different content types.

```bash
curl http://localhost:8080/api/test/transformations/available
```

**What it does**:
- Checks if TransformerService is initialized
- Queries available transformations for common MIME types (PDF, DOCX, HTML, etc.)
- Returns a map of source MIME type → list of target MIME types

**Perfect for**: Verifying that transformer tools are installed and registered.

### 2. Test PDF Transformations

Tests PDF transformation discovery and creates a test document.

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-pdf
```

**What it does**:
1. Checks TransformerService is available
2. Gets a DMS session
3. Queries available PDF transformations
4. Creates a test Document for future transformation testing
5. Returns all discovered transformations

**Perfect for**: Setting breakpoints to single-step through transformation discovery logic.

**Response Example**:
```json
{
  "steps": [
    "✓ TransformerService available",
    "✓ DMS session obtained",
    "✓ Found 3 transformations for PDF",
    "✓ Document created: Document:12345"
  ],
  "source_mime_type": "application/pdf",
  "transformations": [
    {
      "targetMimeType": "image/jpeg",
      "methodName": "pdf_to_image",
      "transformerName": "PdfToImageTransformer"
    },
    {
      "targetMimeType": "image/png",
      "methodName": "pdf_to_image",
      "transformerName": "PdfToImageTransformer"
    }
  ],
  "transformation_count": 2,
  "test_document_guid": "Document:12345",
  "success": true
}
```

### 3. Test DOCX Transformations

Tests Word document transformation discovery.

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-docx
```

**What it does**:
- Checks available transformations for DOCX files
- Returns transformation methods (typically DOCX → PDF via LibreOffice)

### 4. Test HTML Transformations

Tests HTML transformation discovery.

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-html
```

**What it does**:
- Checks available transformations for HTML content
- Returns transformation methods (typically HTML → PDF)

### 5. Debug Info

Get detailed status information about the transformation system.

```bash
curl http://localhost:8080/api/test/transformations/debug
```

**Response Example**:
```json
{
  "transformations": {
    "application/pdf": ["image/jpeg", "image/png", "text/plain"],
    "text/html": ["application/pdf"],
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document": ["application/pdf"]
  },
  "total_source_types": 3
}
```

## Debugging in IDE

### Setting Breakpoints

To single-step through transformation code, set breakpoints at these key locations:

#### 1. Test Controller Entry Point
**File**: `TransformationTestController.java`
**Method**: `testPdfToImage()` (or other test methods)
**Line**: First line of method

#### 2. DMS Session Creation
**File**: `TransformationTestController.java`
**Line**: `session = (DMSSession) DMSSessionFactory.getFactory().getSession();`

#### 3. Document Creation
**File**: `TransformationTestController.java`
**Line**: `session.saveObject(doc);`

#### 4. Transformation Query
**File**: `RenditionTransformationHelper.java` (in hitorro-basedms)
**Method**: `getAvailableTransformations()`

#### 5. Transformation Queue
**File**: `RenditionTransformationHelper.java`
**Method**: `queueTransformation()`

#### 6. Transformation Execution
**File**: Look for transformer implementations like:
- `PdfToImageTransformer.java`
- `OfficeDocumentTransformer.java`
- `HtmlToPdfTransformer.java`

### IntelliJ IDEA Debug Configuration

1. **Create HTTP Request Debug Configuration**:
   - Open the `.http` file or create one:
   ```http
   POST http://localhost:8080/api/test/transformations/pdf-to-image
   Accept: application/json
   ```

2. **Or use cURL in Terminal with Debug Mode**:
   - Start the app in Debug mode in IntelliJ
   - Set breakpoints in `TransformationTestController.testPdfToImage()`
   - Run curl command from terminal
   - IDE will pause at breakpoints

3. **Remote Debug** (if running app externally):
   ```bash
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/hitorro-example-springboot-1.0.0.jar
   ```
   
   Then attach IntelliJ debugger to port 5005.

## Step-by-Step Debugging Workflow

### Example: Debug PDF to Image Transformation

1. **Start the application in debug mode**:
   ```bash
   cd hitorro-example-springboot
   ./run.sh
   ```
   
   Or in IntelliJ: Run → Debug 'HitorroExampleApplication'

2. **Set breakpoint in test controller**:
   - Open `TransformationTestController.java`
   - Click line number to set breakpoint at line: `session = (DMSSession) DMSSessionFactory.getFactory().getSession();`

3. **Trigger the endpoint**:
   ```bash
   curl -X POST http://localhost:8080/api/test/transformations/pdf-to-image
   ```

4. **Step through the code**:
   - IDE will pause at breakpoint
   - Use F8 (Step Over) to go line-by-line
   - Use F7 (Step Into) to dive into method calls
   - Watch variables in Variables panel
   - Check log output

5. **Key things to observe**:
   - **DMS Session**: Is session created successfully?
   - **Document**: Is document saved with correct GUID?
   - **Content**: Is content object created with PDF MIME type?
   - **Transformations Available**: What transformations are found?
   - **Job Creation**: Is TransformationJob created and persisted?
   - **Transformer Tool**: Which tool is being used (pdftoppm, ImageMagick)?

6. **Check database state**:
   - Open H2 Console: http://localhost:8080/h2-console
   - Query transformation jobs:
   ```sql
   SELECT * FROM TRANSFORMATION_JOB;
   SELECT * FROM CONTENT;
   SELECT * FROM DOCUMENT;
   ```

## Common Issues and Solutions

### Issue 1: Transformations Not Available

**Symptom**: `available_transformations` array is empty

**Cause**: Transformer tools not installed on system

**Solution**: Install required tools:
```bash
# macOS
brew install poppler imagemagick libreoffice

# Linux
apt-get install poppler-utils imagemagick libreoffice

# Check installation
pdftoppm --version
convert --version
soffice --version
```

### Issue 2: Job Status Stays QUEUED

**Symptom**: Job never moves from QUEUED to RUNNING

**Cause**: Transformation service not processing queue

**Solution**: Check if transformation service is running:
- Look for transformation worker threads in logs
- Check if `TransformerService` is loaded in service framework
- Verify `hitorro.transformer.enabled=true` in config

### Issue 3: Transformation Fails

**Symptom**: Job status = FAILED

**Solution**: Check job error message:
```bash
curl http://localhost:8080/api/test/transformations/job/1
```

Common errors:
- **Tool not found**: Install the required transformer tool
- **Content not found**: Content GUID is invalid or not saved
- **Store not configured**: No Store configured for content storage
- **Permission denied**: Transformer can't write to temp directory

### Issue 4: Content Has No Actual Bytes

**Symptom**: Content object created but transformation fails because no actual file data

**Cause**: Test endpoints create metadata only, not actual file content

**Solution**: For real testing, use the Document Management endpoints to upload actual files:
```bash
# Upload a real PDF
curl -X POST http://localhost:8080/api/dms/documents \
  -F "file=@test.pdf" \
  -F "name=Real PDF Document"
```

Then use the returned content GUID with transformation endpoints.

## Production Testing

For testing with **real files**:

1. **Upload a real document**:
   ```bash
   curl -X POST http://localhost:8080/api/dms/documents \
     -F "file=@sample.pdf" \
     -F "name=Sample PDF"
   ```

2. **Extract content GUID** from response

3. **Trigger transformation via auto-configured endpoint**:
   ```bash
   curl -X POST http://localhost:8080/api/transformer/queue \
     -H "Content-Type: application/json" \
     -d '{
       "sourceContentGuid": "Content:12345",
       "targetMimeType": "image/jpeg",
       "methodName": "pdf_to_image",
       "methodArgs": "format=jpeg,dpi=150"
     }'
   ```

4. **Monitor job**:
   ```bash
   curl http://localhost:8080/api/test/transformations/job/{jobId}
   ```

## Swagger UI Testing

All test endpoints are available in Swagger UI:

1. Open: http://localhost:8080/swagger-ui.html
2. Find section: **Transformation Tests**
3. Expand endpoint (e.g., "Test PDF to Image transformation")
4. Click **Try it out**
5. Click **Execute**
6. View response

This provides a nice GUI for testing and viewing responses.

## Next Steps

1. **Add Store Configuration**: Configure a file store in `application.yml` so content can be actually saved
2. **Enable Transformation Service**: Ensure transformer service is running to process queued jobs
3. **Add Real File Upload**: Integrate with existing document upload endpoints
4. **Add Monitoring**: Create endpoint to list all transformation jobs and their statuses
5. **Add Polling**: Create endpoint that polls job status until completion

## Architecture Notes

The transformation pipeline:

```
HTTP Request
    ↓
TransformationTestController
    ↓
DMSSession (get/create session)
    ↓
Document (create document entity)
    ↓
Content (create content entity with MIME type)
    ↓
RenditionTransformationHelper.getAvailableTransformations()
    ↓
TransformationRegistry (find matching transformer)
    ↓
RenditionTransformationHelper.queueTransformation()
    ↓
TransformationJob (create and persist job)
    ↓
TransformationService (background processing)
    ↓
Specific Transformer (PdfToImageTransformer, etc.)
    ↓
External Tool (pdftoppm, ImageMagick, LibreOffice)
    ↓
Result Content (new content with transformed data)
    ↓
TransformationJob.status = COMPLETED
```

Perfect for setting breakpoints at each stage!
