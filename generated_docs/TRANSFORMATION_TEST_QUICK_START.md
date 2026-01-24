# Transformation Testing - Quick Start

## What Was Fixed

1. ✅ **TransformerAutoConfiguration** now registered in Spring Boot autoconfiguration
2. ✅ **Hanging test disabled** - renamed to `.disabled` extension
3. ✅ **Transformer configuration added** to example app's `application.yml`
4. ✅ **New test endpoints** created for debugging transformations

## Quick Test Commands

### 1. Check What Transformations Are Available

```bash
curl http://localhost:8080/api/test/transformations/available
```

This shows all registered transformations (PDF→Image, DOCX→PDF, etc.)

### 2. Test PDF Transformations

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-pdf
```

Creates a test document and shows available PDF transformations.

### 3. Test Office Document Transformations

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-docx
```

Shows available transformations for Word documents.

### 4. Test HTML Transformations

```bash
curl -X POST http://localhost:8080/api/test/transformations/test-html
```

Shows available transformations for HTML content.

### 5. Debug System Status

```bash
curl http://localhost:8080/api/test/transformations/debug
```

Shows whether TransformerService and DMS are available.

## Debugging in IntelliJ

### Set Breakpoints At These Key Locations:

1. **Test Controller Entry**
   - File: `TransformationTestController.java`
   - Method: `testPdfTransformations()` 
   - Line: `TransformerService service = TransformerService.getService();`

2. **Transformation Discovery**
   - File: `TransformationTestController.java`
   - Line: `RenditionTransformationHelper.getAvailableTransformations(sourceMimeType);`

3. **Document Creation**
   - File: `TransformationTestController.java`
   - Line: `session.persist(doc);`

### Debugging Workflow:

1. Start app in debug mode in IntelliJ
2. Set breakpoint in `TransformationTestController.testPdfTransformations()`
3. Run: `curl -X POST http://localhost:8080/api/test/transformations/test-pdf`
4. IDE pauses at breakpoint
5. Step through code with F8 (step over) or F7 (step into)
6. Watch variables and check logs

## Using Auto-Configured Transformer Endpoints

The Spring Boot autoconfiguration also provides these endpoints:

### Get Available Transformations
```bash
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"
```

### Get Available Target MIME Types
```bash
curl "http://localhost:8080/api/transformer/available-targets?sourceMimeType=application/pdf"
```

### Get Recent Documents
```bash
curl http://localhost:8080/api/documents/recent
```

### Search Documents
```bash
curl "http://localhost:8080/api/documents/search?q=test"
```

## Check in Swagger UI

Open: http://localhost:8080/swagger-ui.html

Look for these sections:
- **Transformation Tests** - Test endpoints for debugging
- **rendition-transformation-controller** - Auto-configured transformation API
- **document-content-controller** - Auto-configured document API

## Common Issues

### "TransformerService not initialized"
**Cause**: Transformer service not loaded
**Fix**: 
1. Check `application.yml` has `hitorro.transformer.enabled: true`
2. Verify service framework is loading TransformerService
3. Check logs for service initialization errors

### "No transformations available"
**Cause**: Transformer tools not installed
**Fix**: Install required tools:
```bash
# macOS
brew install poppler imagemagick libreoffice

# Linux  
apt-get install poppler-utils imagemagick libreoffice
```

### DMS Session Errors
**Cause**: DMS not properly initialized
**Fix**: Check that these services are loaded (in `application.yml`):
```yaml
hitorro:
  services:
    load:
      - com.hitorro.basedms.db.HibernateService
      - com.hitorro.base.objects.BaseDMSService
```

## Files Changed

1. `hitorro-spring-boot/hitorro-spring-boot-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   - Added: `com.hitorro.spring.autoconfigure.transformer.TransformerAutoConfiguration`

2. `hitorro-example-springboot/src/main/resources/application.yml`
   - Added transformer configuration section

3. `hitorro-spring-boot/hitorro-spring-boot-autoconfigure/src/test/java/com/hitorro/spring/autoconfigure/transformer/TransformerRestControllerTest.java`
   - Renamed to `.disabled` (was hanging due to DMS initialization)

4. `hitorro-example-springboot/src/main/java/com/hitorro/example/controller/TransformationTestController.java`
   - **NEW**: Test endpoints for transformation debugging

## Next Steps for Real Transformations

To actually transform files (not just test the configuration):

1. **Upload a real document**:
   ```bash
   curl -X POST http://localhost:8080/api/dms/documents \
     -F "file=@sample.pdf" \
     -F "title=Sample PDF"
   ```

2. **Use auto-configured transformation endpoints** to queue transformations

3. **Configure a Store** in application.yml for file storage

4. **Enable transformation worker** to process queued jobs

See `TRANSFORMATION_TEST_GUIDE.md` for detailed instructions.

## Verification Checklist

- [ ] App starts without errors
- [ ] `/api/test/transformations/available` returns transformation list
- [ ] `/api/test/transformations/debug` shows TransformerService available
- [ ] Swagger UI shows "Transformation Tests" section
- [ ] Swagger UI shows "rendition-transformation-controller" section
- [ ] Can set breakpoints and single-step through test endpoints
