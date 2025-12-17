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
package ht.util.basefile.fs;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import ht.jsontypesystem.JVS;
import ht.util.core.string.Fmt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FileInfo {
    BaseFile bf;
    String md5;
    String name;
    long modified;

    public FileInfo(BaseFile bf) throws IOException {
        this.bf = bf;
        md5 = bf.getDigest();
        name = bf.getName();
        modified = bf.getModifiedTime();
    }

    private void foo() throws IOException {
        BufferedImage image = ImageIO.read(bf.getDataInputStream());
        if (image != null && image.getPropertyNames() != null) {
            for (int j = 0; j < image.getPropertyNames().length; j++) {
                String key = image.getPropertyNames()[j];
                String value = (String) image.getProperty(key);
                System.out.println(key + ": " + value);
            }
        }
    }

    public JVS getJVS() {

        JVS jvs = new JVS();
        jvs.set("file", bf.getAbsolutePath());
        jvs.set("md5", md5);
        jvs.set("name", name);
        jvs.set("modified", modified);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(bf.getDataInputStream(), bf.length());
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    jvs.set(Fmt.S("exif.%s.%s", directory.getName(), tag.getTagName()), tag.getDescription());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }
        return jvs;
    }
}