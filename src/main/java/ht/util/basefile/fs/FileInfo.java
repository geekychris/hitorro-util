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