# Transformer - Quick Reference Card

## 🚀 Start the App

```bash
# Terminal 1 - Backend
cd hitorro-example-springboot
./mvnw spring-boot:run

# Terminal 2 - Frontend  
cd hitorro-example-springboot/react-app
npm run dev
```

Then open: `http://localhost:3000`

## 🔄 Transform Content (UI)

1. **Document Management** tab (default)
2. Select a document
3. Click purple **"Transform"** button on content
4. Choose format
5. Confirm

## 📡 Transform Content (API)

### Get Available Transformations
```bash
curl "http://localhost:8080/api/transformer/content/{contentGuid}/available-transformations"
```

### Queue Transformation
```bash
curl -X POST "http://localhost:8080/api/transformer/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceContentGuid": "abc123",
    "targetMimeType": "image/jpeg",
    "method": "pdf_to_image",
    "transformer": "pdf2image",
    "methodArgs": "format=jpeg;dpi=150;quality=85"
  }'
```

## 🛠️ Install Dependencies (One Time)

```bash
# Automatic (detects OS)
./scripts/install-transformer-dependencies.sh

# Verify
./scripts/test-transformer-setup.sh

# Manual (Ubuntu/Debian)
sudo apt-get install poppler-utils libreoffice imagemagick

# Manual (macOS)
brew install poppler libreoffice imagemagick
```

## 📦 Supported Transformations

### PDF → Image
- PDF → JPEG (configurable DPI, quality)
- PDF → PNG (configurable DPI)
- PDF → TIFF (configurable DPI)

### Office → PDF
- Word (.docx, .doc, .odt) → PDF
- Excel (.xlsx, .xls, .ods) → PDF
- PowerPoint (.pptx, .ppt, .odp) → PDF

### Image Conversions
- Any image format → JPEG/PNG/GIF/BMP/TIFF
- Resize support with dimensions

### Video (if Sorenson Squeeze installed)
- Any video → MP4 (iPod format)

## 🗂️ Key Files

### Implementation
- `PDFToImageTransformer.java` - PDF conversions
- `LibreOfficeTransformer.java` - Office conversions
- `ImageMagickTransformer.java` - Image conversions
- `RenditionTransformationController.java` - REST API
- `DocumentContentController.java` - Document API

### Configuration
- `edges.csv` - Transformation routing
- `application.properties` - Tool paths (if needed)

### UI
- `DMSPageEnhanced.tsx` - Transform button & modal

### Scripts
- `install-transformer-dependencies.sh` - Setup
- `test-transformer-setup.sh` - Verify
- `create-test-documents.sh` - Test data

## 🔍 Troubleshooting

### "No transformations available"
→ Install dependencies: `./scripts/install-transformer-dependencies.sh`

### Transformation fails
→ Check logs: `tail -f logs/hitorro.log`
→ Verify tools: `./scripts/test-transformer-setup.sh`

### Can't find content
→ Upload a document with content first
→ Check document has contentCount > 0

### Job queued but not processing
→ Check TransformerService is running
→ Check job queue: Look in database or logs

## 📚 Documentation

- **Quick Start**: `TRANSFORMER_QUICK_START.md`
- **UI Integration**: `TRANSFORMER_UI_INTEGRATION_COMPLETE.md`
- **Implementation**: `TRANSFORMER_IMPLEMENTATION_GUIDE.md`
- **Full Summary**: `TRANSFORMER_FINAL_SUMMARY.md`

## 💡 Tips

- **Batch**: Queue multiple transformations - they process concurrently
- **Tags**: Use tags to track transformation jobs
- **Renditions**: Transformed content appears as rendition of source
- **Parameters**: Customize DPI, quality, dimensions via methodArgs
- **Availability**: Only installed transformers show in UI

## 🎯 Common Use Cases

### Convert PDF to thumbnails
Method: `pdf_to_image`
Args: `format=jpeg;dpi=72;quality=80`

### Convert Word to PDF
Method: `document_to_pdf`
Target: `application/pdf`

### Resize images
Method: `image_convert`
Args: `format=jpeg;width=800;height=600;quality=90`

## ⚡ Quick Commands

```bash
# Full rebuild
cd hitorro-spring-boot && mvn clean install

# Run tests
cd hitorro-spring-boot && mvn test

# Check available transformations for PDF
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"

# Search documents
curl "http://localhost:8080/api/documents/search?q=test"

# Get document content
curl "http://localhost:8080/api/documents/{guid}/content"
```

---

**Need Help?** Check `TRANSFORMER_FINAL_SUMMARY.md` for complete details.
