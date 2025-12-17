package ht.util.basefile.tools.queue.reader;

import ht.util.core.Log;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

public class GoodFileKeyWriter {
    private File parentFile;
    private File keyFile;
    private String goodKey;

    public GoodFileKeyWriter() {

    }

    public GoodFileKeyWriter(File keyFile) {
        this.keyFile = keyFile;
        parentFile = keyFile.getParentFile();
        if (keyFile.exists()) {
            StringBuilder sb = FileUtil.readFromFile(keyFile);
            if (sb != null) {
                goodKey = sb.toString();
            }
        }
    }

    public String getSource() {
        return keyFile.getAbsolutePath();
    }

    public String[] getParts() {
        if (StringUtil.nullOrEmptyString(goodKey)) {
            return new String[0];
        }
        return StringUtil.tokenizeFromSingleChar(goodKey, "/");
    }

    public String getGoodKey() {
        return goodKey;
    }


    public Date getLastGoodKeyDate() {
        return null;
    }

    public void reset() {
        goodKey = null;
        if (FileUtil.notNullAndExists(keyFile)) {
            keyFile.delete();
        }
    }

    public void setName(File file) {
        keyFile = file;
        if (keyFile.exists()) {
            StringBuilder sb = FileUtil.readFromFile(keyFile);
            if (sb != null) {
                goodKey = sb.toString();
            }
        }
    }

    public void save(String key) {
        goodKey = key;
        try {
            FileUtil.ensureDirectoryExists(parentFile);
            FileUtil.writeStringToFile(keyFile, key);
        } catch (FileNotFoundException e) {
            Log.util.error("GoodFileKeyWriter unable to write last good key %s %e", e, e);
        }
    }
}
