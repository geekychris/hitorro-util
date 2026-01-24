# Transformer Fixes and Available Transformations

## ✅ Issues Fixed

### 1. **edges.csv Transformer Names Mismatch**

**Problem**: The `edges.csv` file was using incorrect transformer names that didn't match the registered method names.

**What was wrong**:
```csv
# WRONG - these names don't exist
application/pdf, image/jpeg, pdf_converter, pdf_to_image, ...
application/msword, application/pdf, libreoffice, libreoffice_convert, ...
```

**Fixed**:
```csv
# CORRECT - matches registered methods
application/pdf, image/jpeg, pdf2image, pdf_to_image, ...
application/msword, application/pdf, libreoffice, libreoffice_convert, ...
```

The transformer name (column 3) is just a label, but it needs to be consistent. The **method name** (column 4) must exactly match what's registered in `TransformerService.java`.

### 2. **Missing Transformer Name in API Response**

**Problem**: The REST API wasn't returning the transformer name, only the method name.

**Fixed**:
- Updated `TransformationInfo` class to include `transformerName` field
- Updated REST API to return both `transformer` and `method` fields
- Updated UI to use correct field names

### 3. **Added More Transformation Options**

Added additional transformations:
- PDF → TIFF (high-resolution 300 DPI)
- Images → GIF, BMP, TIFF
- Text → PDF

---

## 📋 Complete List of Supported Transformations

### For Your Specific File Types

#### **PDF Files** → 3 Transformations Available

| Target Format | MIME Type | Parameters |
|--------------|-----------|------------|
| JPEG Image | `image/jpeg` | DPI: 150, Quality: 85 |
| PNG Image | `image/png` | DPI: 150 |
| TIFF Image | `image/tiff` | DPI: 300 (high quality) |

**Tool Required**: `pdftoppm` (from poppler-utils)

---

#### **JPEG Files** → 5 Transformations Available

| Target Format | MIME Type | Parameters |
|--------------|-----------|------------|
| PNG | `image/png` | Lossless |
| GIF | `image/gif` | Standard |
| BMP | `image/bmp` | Uncompressed |
| TIFF | `image/tiff` | High quality |
| JPEG | `image/jpeg` | Re-encode with quality 85 |

**Tool Required**: `convert` (from ImageMagick)

---

#### **TXT Files** → 1 Transformation Available

| Target Format | MIME Type | Notes |
|--------------|-----------|-------|
| PDF | `application/pdf` | Converted via LibreOffice |

**Tool Required**: `soffice` (from LibreOffice)

---

#### **DOCX Files** → 1 Transformation Available

| Target Format | MIME Type | Notes |
|--------------|-----------|-------|
| PDF | `application/pdf` | Preserves formatting |

**Tool Required**: `soffice` (from LibreOffice)

---

## 📊 Full Transformation Matrix

### All Supported Source Types

| Source MIME Type | Source Extension | Available Targets |
|-----------------|------------------|-------------------|
| `application/pdf` | .pdf | JPEG, PNG, TIFF |
| `image/jpeg` | .jpg, .jpeg | PNG, GIF, BMP, TIFF |
| `image/png` | .png | JPEG, GIF, BMP, TIFF |
| `image/gif` | .gif | JPEG, PNG, BMP, TIFF |
| `image/bmp` | .bmp | JPEG, PNG, GIF, TIFF |
| `image/tiff` | .tif, .tiff | JPEG, PNG, GIF, BMP |
| `text/plain` | .txt | PDF |
| `application/msword` | .doc | PDF |
| `application/vnd.openxmlformats-officedocument.wordprocessingml.document` | .docx | PDF |
| `application/vnd.ms-excel` | .xls | PDF |
| `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | .xlsx | PDF |
| `application/vnd.ms-powerpoint` | .ppt | PDF |
| `application/vnd.openxmlformats-officedocument.presentationml.presentation` | .pptx | PDF |
| `application/vnd.oasis.opendocument.text` | .odt | PDF |
| `application/vnd.oasis.opendocument.spreadsheet` | .ods | PDF |
| `application/vnd.oasis.opendocument.presentation` | .odp | PDF |

**Total**: **23 transformation edges** configured

---

## 🔧 Installation Instructions

If you see "No transformations available" in the UI, install the required tools:

### Quick Install (Automatic)

```bash
cd /Users/chris/hitorro
./scripts/install-transformer-dependencies.sh
```

### Manual Install

#### Ubuntu/Debian
```bash
sudo apt-get update
sudo apt-get install -y poppler-utils libreoffice imagemagick
```

#### macOS
```bash
brew install poppler libreoffice imagemagick
```

#### RHEL/CentOS/Fedora
```bash
sudo dnf install -y poppler-utils libreoffice ImageMagick
```

---

## ✅ Verify Installation

```bash
cd /Users/chris/hitorro
./scripts/test-transformer-setup.sh
```

Expected output:
```
Testing pdftoppm (PDF to Image)... ✓ PASS
Testing LibreOffice (Document to PDF)... ✓ PASS
Testing ImageMagick (Image Conversion)... ✓ PASS

All transformer tools are properly installed!
```

---

## 🚀 After Installation

1. **Restart the backend** to detect new tools:
   ```bash
   # Stop the backend (Ctrl+C), then:
   cd hitorro-example-springboot
   ./mvnw spring-boot:run
   ```

2. **Check the logs** for registration messages:
   ```
   Registered transformer method: pdf_to_image
   Registered transformer method: libreoffice_convert
   Registered transformer method: imagemagick_convert
   ```

3. **Test in the UI**:
   - Open `http://localhost:3000`
   - Go to Document Management
   - Select a document with PDF content
   - Click the purple **"Transform"** button
   - You should now see 3 transformation options!

---

## 🐛 Troubleshooting

### "No transformations available" even after installing tools

**Check 1**: Verify tools are in PATH
```bash
which pdftoppm soffice convert
```

**Check 2**: Check backend logs for warnings
```bash
tail -f logs/hitorro.log | grep transformer
```

**Check 3**: Test API directly
```bash
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"
```

Should return JSON with `"available": true`.

### Transformation fails or times out

**Check**: Tool is working manually
```bash
# Test PDF to image
pdftoppm -jpeg test.pdf test

# Test LibreOffice  
soffice --headless --convert-to pdf test.docx

# Test ImageMagick
convert test.jpg test.png
```

### Tools installed but not detected

**Solution**: Set explicit paths in `application.properties`:
```properties
transformer.pdftoppm.path=/usr/bin/pdftoppm
transformer.libreoffice.path=/usr/bin/soffice
transformer.imagemagick.path=/usr/bin/convert
```

---

## 📝 API Examples

### Get transformations for PDF
```bash
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"
```

### Get transformations for specific content
```bash
curl "http://localhost:8080/api/transformer/content/Content:12345/available-transformations"
```

### Queue a transformation
```bash
curl -X POST "http://localhost:8080/api/transformer/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceContentGuid": "Content:12345",
    "targetMimeType": "image/jpeg",
    "methodName": "pdf_to_image",
    "methodArgs": "format=jpeg,dpi=150,quality=85"
  }'
```

---

## 📖 Summary

### What Was Fixed
1. ✅ Corrected transformer names in `edges.csv`
2. ✅ Added transformer name to API response
3. ✅ Updated UI to use correct API field names
4. ✅ Added more transformation options (TIFF, GIF, BMP, TXT→PDF)
5. ✅ Rebuilt all modules successfully

### What You Can Transform Now

**Your files**:
- **PDF** → JPEG, PNG, TIFF (3 options)
- **JPEG** → PNG, GIF, BMP, TIFF, JPEG (5 options)
- **TXT** → PDF (1 option)
- **DOCX** → PDF (1 option)

**Total**: **10 transformation options** for your specific file types

### Next Steps
1. Install tools: `./scripts/install-transformer-dependencies.sh`
2. Restart backend
3. Try transforming a PDF to JPEG in the UI

The transformer should now work perfectly! 🎉
