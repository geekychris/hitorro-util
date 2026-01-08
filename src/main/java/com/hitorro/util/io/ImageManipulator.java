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
package com.hitorro.util.io;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class ImageManipulator {
    private int imageWidth;
    private int imageHeight;
    private BufferedImage imageBuffer;

    public static final boolean convert(String inputPath, String outputPath, String targetFormat, int maxwidth, int maxHeight) throws IOException {
        ImageManipulator man = new ImageManipulator();
        BaseFile in = FileFileSystem.Root.getFile(inputPath);
        if (in.exists()) {
            BaseFile out = FileFileSystem.Root.getFile(outputPath);
            out.mkParentDir();
            man.setInputFile(in);
            man.convert(out, maxwidth, maxHeight, targetFormat);
            return true;
        }
        return false;
    }

    public static void main(String args[]) {
        String in = "/Users/chriscollins/in.jpg";
        String out = "/Users/chriscollins/out.jpg";
        try {
            ImageManipulator.convert(in, out, "jpeg", 150, 150);
        } catch (Exception e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void readMetaData(BaseFile srcImageFile) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(srcImageFile.getDataInputStream());
        ImageReader reader = ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);
        IIOImage srcIIOImage = reader.readAll(0, null);
        IIOMetadata meta = srcIIOImage.getMetadata();
        String names[] = meta.getMetadataFormatNames();
    }

    public void setInputFile(BaseFile input) throws IOException {
        imageBuffer = javax.imageio.ImageIO.read(input.getDataInputStream());

        /* determine thumbnail size from WIDTH and HEIGHT */
        imageWidth = imageBuffer.getWidth(null);
        imageHeight = imageBuffer.getHeight(null);
        //readMetaData(input);
    }

    //"png"
    public void convert(BaseFile output, int width, int height, String outputFormat) throws IOException {
        int imageBufferWidth;
        int imageBufferHeight;
        BufferedImage image = null;
        int x = 0;
        int y = 0;

        if (imageWidth < imageHeight) {
            imageBufferWidth = width;
            imageBufferHeight = (int) (((double) imageHeight * width) / imageWidth);
            y = -(imageBufferHeight - imageBufferWidth) / 2;
        } else {
            imageBufferHeight = height;
            imageBufferWidth = (int) (((double) imageWidth * height) / imageHeight);
            x = -(imageBufferWidth - imageBufferHeight) / 2;
        }
        Image imageBuffer1 = imageBuffer.getScaledInstance(imageBufferWidth, imageBufferHeight, Image.SCALE_SMOOTH);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(imageBuffer1, x, y, null);
        String informalNames[] = javax.imageio.ImageIO.getReaderFormatNames();
        for (String name : informalNames) {
            Console.println("Reader name: %s", name);
        }

        informalNames = javax.imageio.ImageIO.getWriterFormatNames();
        for (String name : informalNames) {
            Console.println("Writer name: %s", name);
        }

        javax.imageio.ImageIO.write(image, outputFormat, output.getDataOutputStream());
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
}
