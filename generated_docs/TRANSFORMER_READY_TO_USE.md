# Transformer - Ready to Use!

## ✅ Status: Almost Ready!

### What's Installed
- ✅ **pdftoppm** (Poppler) - For PDF → Image conversions
- ✅ **LibreOffice 25.2** - For Office/Text → PDF conversions
- ✅ **ImageMagick** - For image conversions
- ✅ **Spring Boot** - Transformer API registered and built

### What's Needed (1 Quick Fix)

**LibreOffice is installed but `soffice` is not in your PATH.**

The `soffice` command is buried inside the LibreOffice.app bundle and needs to be made accessible.

---

## 🚀 Quick Fix (30 seconds)

Run this script to set up the PATH:

```bash
cd /Users/chris/hitorro
./setup-soffice-path.sh
```

This will:
1. Create a symlink from `/Applications/LibreOffice.app/Contents/MacOS/soffice` to `/usr/local/bin/soffice`
2. Make `soffice` command accessible system-wide
3. Verify it's working

**You'll be prompted for your password** (standard for creating system-wide symlinks).

---

## Alternative: Manual PATH Setup

If you prefer not to create a symlink, add LibreOffice to your PATH:

```bash
# Edit your shell config
nano ~/.zshrc

# Add this line at the end:
export PATH="/Applications/LibreOffice.app/Contents/MacOS:$PATH"

# Save and reload
source ~/.zshrc

# Verify
which soffice
soffice --version
```

---

## After Setup: Restart Backend

Once `soffice` is accessible:

```bash
# 1. Stop the current Spring Boot app (Ctrl+C in terminal or stop in IDE)

# 2. Restart it
cd /Users/chris/hitorro/hitorro-example-springboot
./mvnw spring-boot:run

# 3. Look for these lines in the logs:
#    ✓ Registered transformer method: pdf_to_image
#    ✓ Registered transformer method: libreoffice_convert
#    ✓ Registered transformer method: imagemagick_convert
```

---

## Test It Works

### Test 1: Verify Tools (Before Restart)

```bash
cd /Users/chris/hitorro
./scripts/test-transformer-setup.sh
```

**Expected output:**
```
Testing pdftoppm (PDF to Image)... ✓ PASS
Testing LibreOffice (Document to PDF)... ✓ PASS
Testing ImageMagick (Image Conversion)... ✓ PASS

All transformer tools are properly installed!
```

### Test 2: Test API (After Restart)

```bash
curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"
```

**Expected output:** JSON with 3 transformations (JPEG, PNG, TIFF)

### Test 3: Test UI (After Restart)

1. Open `http://localhost:3000`
2. Go to **Document Management**
3. Select a **PDF document**
4. Click the purple **"Transform"** button
5. You should see **3 transformation options**!

---

## What Transformations Will Work

### Your Specific Files

Once setup is complete, you can transform:

#### **PDF Files** (3 transformations)
- ✅ PDF → JPEG (150 DPI, quality 85)
- ✅ PDF → PNG (150 DPI)
- ✅ PDF → TIFF (300 DPI, high quality)

#### **JPEG Files** (5 transformations)
- ✅ JPEG → PNG
- ✅ JPEG → GIF
- ✅ JPEG → BMP
- ✅ JPEG → TIFF
- ✅ JPEG → JPEG (re-encode with different quality)

#### **TXT Files** (1 transformation)
- ✅ TXT → PDF (via LibreOffice)

#### **DOCX Files** (1 transformation)
- ✅ DOCX → PDF (via LibreOffice)

**Total: 10 transformations** for your specific file types

### All Files (Complete List)

Once all tools are working, you'll have **23 transformation edges** supporting:
- PDF → Images (JPEG, PNG, TIFF)
- Office documents → PDF (Word, Excel, PowerPoint, OpenDocument)
- Text → PDF
- Images → Other image formats

See `TRANSFORMATION_MATRIX.md` for the complete list.

---

## Current Status Summary

| Tool | Status | Notes |
|------|--------|-------|
| pdftoppm | ✅ READY | In PATH at `/opt/homebrew/bin/pdftoppm` |
| ImageMagick | ✅ READY | In PATH at `/opt/homebrew/bin/convert` |
| LibreOffice | ⚠️ **NEEDS PATH** | Installed but not in PATH |
| Spring Boot | ✅ READY | TransformerAutoConfiguration registered |
| Backend | ⚠️ **NEEDS RESTART** | Must restart after soffice is in PATH |

---

## Quick Action Plan

```bash
# Step 1: Fix soffice PATH (30 seconds)
cd /Users/chris/hitorro
./setup-soffice-path.sh

# Step 2: Verify all tools (10 seconds)
./scripts/test-transformer-setup.sh

# Step 3: Stop backend (5 seconds)
# Press Ctrl+C in the terminal running Spring Boot
# OR stop it in your IDE

# Step 4: Restart backend (30 seconds)
cd /Users/chris/hitorro/hitorro-example-springboot
./mvnw spring-boot:run

# Step 5: Test in UI (1 minute)
# Open http://localhost:3000
# Document Management → Select PDF → Click "Transform"
# Should see 3 options!
```

**Total time: ~2 minutes**

---

## Troubleshooting

### "soffice: command not found" after running setup script

**Solution 1**: Check if symlink was created
```bash
ls -la /usr/local/bin/soffice
```

**Solution 2**: Use the manual PATH method (see above)

**Solution 3**: Use the full path in application.properties
```properties
# Add to hitorro-example-springboot/src/main/resources/application.properties
transformer.libreoffice.path=/Applications/LibreOffice.app/Contents/MacOS/soffice
```

### Backend still shows "LibreOffice transformer unavailable"

1. Verify `soffice` is in PATH: `which soffice`
2. Make sure you restarted the backend AFTER fixing the PATH
3. Check backend logs for the exact error message

### Transformations still show as "not available" in UI

1. Check API directly: `curl "http://localhost:8080/api/transformer/transformations?sourceMimeType=application/pdf"`
2. If API returns empty or 404: Backend not restarted or TransformerAutoConfiguration not loaded
3. If API returns transformations with `"available": false`: Tools not accessible to Java process

---

## Summary

**You're 99% there!** All tools are installed. Just need to:

1. ✅ Run `./setup-soffice-path.sh` (or manually add to PATH)
2. ✅ Restart the backend
3. ✅ Test in UI

That's it! 🎉
