# Hitorro Transformer - Supported Transformations Matrix

## Overview

This document lists **all supported content transformations** based on your current configuration.

## Current Configuration Status

The transformer framework supports the following transformations based on installed tools:

### ✅ PDF Transformations (requires `pdftoppm`)

**Source MIME Type**: `application/pdf`

| Target Format | Target MIME Type | Method | Parameters |
|--------------|------------------|---------|------------|
| JPEG Image | `image/jpeg` | `pdf_to_image` | format=jpeg, dpi=150, quality=85 |
| PNG Image | `image/png` | `pdf_to_image` | format=png, dpi=150 |
| TIFF Image | `image/tiff` | `pdf_to_image` | format=tiff, dpi=300 |

**Install**: `sudo apt-get install poppler-utils` (or `brew install poppler` on macOS)

---

### ✅ Office Document → PDF (requires LibreOffice)

**Supported Source Formats**:

| Source Format | Source MIME Type | Target | Method |
|--------------|------------------|--------|---------|
| Word (.doc) | `application/msword` | PDF | `libreoffice_convert` |
| Word (.docx) | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` | PDF | `libreoffice_convert` |
| Excel (.xls) | `application/vnd.ms-excel` | PDF | `libreoffice_convert` |
| Excel (.xlsx) | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | PDF | `libreoffice_convert` |
| PowerPoint (.ppt) | `application/vnd.ms-powerpoint` | PDF | `libreoffice_convert` |
| PowerPoint (.pptx) | `application/vnd.openxmlformats-officedocument.presentationml.presentation` | PDF | `libreoffice_convert` |
| OpenDocument Text (.odt) | `application/vnd.oasis.opendocument.text` | PDF | `libreoffice_convert` |
| OpenDocument Spreadsheet (.ods) | `application/vnd.oasis.opendocument.spreadsheet` | PDF | `libreoffice_convert` |
| OpenDocument Presentation (.odp) | `application/vnd.oasis.opendocument.presentation` | PDF | `libreoffice_convert` |
| Plain Text (.txt) | `text/plain` | PDF | `libreoffice_convert` |

**Target MIME Type**: `application/pdf`  
**Parameters**: `format=pdf`

**Install**: `sudo apt-get install libreoffice` (or `brew install libreoffice` on macOS)

---

### ✅ Image Conversions (requires ImageMagick)

**Universal Image Conversions** (from any image/* type):

| Source | Target Format | Target MIME Type | Method | Parameters |
|--------|--------------|------------------|---------|------------|
| Any Image | JPEG | `image/jpeg` | `imagemagick_convert` | format=jpg, quality=85 |
| Any Image | PNG | `image/png` | `imagemagick_convert` | format=png |
| Any Image | GIF | `image/gif` | `imagemagick_convert` | format=gif |
| Any Image | BMP | `image/bmp` | `imagemagick_convert` | format=bmp |
| Any Image | TIFF | `image/tiff` | `imagemagick_convert` | format=tiff |

**Specific Image Conversions**:

| Source Format | Source MIME | Target Format | Target MIME | Method | Parameters |
|--------------|-------------|---------------|-------------|---------|------------|
| JPEG | `image/jpeg` | PNG | `image/png` | `imagemagick_convert` | format=png |
| PNG | `image/png` | JPEG | `image/jpeg` | `imagemagick_convert` | format=jpg, quality=90 |

**Install**: `sudo apt-get install imagemagick` (or `brew install imagemagick` on macOS)

---

### 📹 Video/Audio Conversions (requires Sorenson Squeeze)

**Note**: These require commercial software (Sorenson Squeeze) which is typically not installed.

| Source | Target Format | Target MIME Type | Transformer | Method |
|--------|--------------|------------------|-------------|---------|
| Any Video | MP4 (iPod) | `video/mp4` | sorenson | `ipod_mp4` |
| Any Video | Flash | `application/x-shockwave-flash` | sorenson | `512K_FLV` |
| Any Audio | MP3 | `audio/mpeg` | sorenson | `128K_MP3` |

---

## Quick Reference by File Type

### Your Specific Question

You mentioned you have **PDF, JPEG, TXT, and DOCX** files. Here's what you can transform them to:

#### PDF → (3 options)
- ✅ JPEG image (150 DPI, quality 85)
- ✅ PNG image (150 DPI)
- ✅ TIFF image (300 DPI, high quality)

#### JPEG → (5 options)
- ✅ PNG
- ✅ GIF
- ✅ BMP
- ✅ TIFF
- ✅ JPEG (different quality settings)

#### TXT → (1 option)
- ✅ PDF (via LibreOffice)

#### DOCX → (1 option)
- ✅ PDF (via LibreOffice)

---

## Why "No Transformations Available"?

If the UI shows "no transformations available," it means:

### 1. **Tools Not Installed**

The transformation tools might not be installed on your system:

```bash
# Check if tools are available
which pdftoppm      # For PDF transformations
which soffice       # For Office/Text transformations
which convert       # For image transformations

# Or run the test script
./scripts/test-transformer-setup.sh
```

### 2. **Install Missing Tools**

```bash
# Automatic installation (detects your OS)
./scripts/install-transformer-dependencies.sh

# Or manual installation:
# Ubuntu/Debian:
sudo apt-get install poppler-utils libreoffice imagemagick

# macOS:
brew install poppler libreoffice imagemagick
```

### 3. **Backend Not Restarted**

After installing tools, restart the backend:

```bash
# Stop the backend (Ctrl+C), then restart:
cd hitorro-example-springboot
./mvnw spring-boot:run
```

### 4. **Check Transformer Registration**

Look for these lines in the startup logs:

```
Registered transformer method: pdf_to_image
Registered transformer method: libreoffice_convert  
Registered transformer method: imagemagick_convert
```

If you see warnings like "PDF to image transformer unavailable", the tools aren't found.

---

## Testing Transformations

### Via UI

1. Upload a PDF document to DMS
2. Select the document
3. Click purple **"Transform"** button on the content
4. You should see 3 options (JPEG, PNG, TIFF)

### Via API

```bash
# Check what's available for PDF
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"

# Should return:
{
  "transformations": [
    {
      "targetMimeType": "image/jpeg",
      "methodName": "pdf_to_image",
      "methodArgs": "format=jpeg,dpi=150,quality=85",
      "available": true
    },
    {
      "targetMimeType": "image/png",
      "methodName": "pdf_to_image",
      "methodArgs": "format=png,dpi=150",
      "available": true
    },
    {
      "targetMimeType": "image/tiff",
      "methodName": "pdf_to_image",
      "methodArgs": "format=tiff,dpi=300",
      "available": true
    }
  ]
}
```

---

## Transformation Workflow

### 1. Source Content → Target Format

```
PDF Document
   ↓ (pdf_to_image)
JPEG Image (150 DPI, quality 85)
```

### 2. Chained Transformations

You can chain transformations:

```
Word Document (.docx)
   ↓ (libreoffice_convert)
PDF Document
   ↓ (pdf_to_image)
PNG Image
```

### 3. Result Storage

Transformed content is stored as a **rendition** of the original:

```
Document: "My Report"
  ├─ Content: report.docx (original)
  └─ Renditions:
       ├─ report.pdf (from transformation)
       └─ report.png (from transformation)
```

---

## Custom Transformations

### Add New Transformation Edge

Edit `/Users/chris/hitorro/data/transcoder/edges.csv`:

```csv
MimeFrom,MimeTo,Transformer,Method,MethodArgs
application/pdf,image/jpeg,pdf2image,pdf_to_image,"format=jpeg,dpi=300,quality=95"
```

Parameters:
- **MimeFrom**: Source MIME type (or `*` for wildcard)
- **MimeTo**: Target MIME type
- **Transformer**: Transformer name (just for reference)
- **Method**: Method name (must match registered method)
- **MethodArgs**: Comma-separated parameters

Restart the backend to pick up changes.

---

## Summary

**Total Supported Transformations**: **24+** transformation paths

**For your specific files**:
- **PDF**: 3 transformations (JPEG, PNG, TIFF)
- **JPEG**: 5 transformations (PNG, GIF, BMP, TIFF, JPEG)
- **TXT**: 1 transformation (PDF)
- **DOCX**: 1 transformation (PDF)

**Next Steps**:
1. Run `./scripts/install-transformer-dependencies.sh`
2. Run `./scripts/test-transformer-setup.sh` to verify
3. Restart backend
4. Try transforming in the UI

If you still see "no transformations available" after installing tools, check the backend logs for warnings about unavailable transformers.
