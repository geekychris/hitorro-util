package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTAssert;
import ht.util.io.FileUtil;

/**
 * User: chris
 */
public class FileExtension extends FileFilterBase {

    public static final FileExtension Json = new FileExtension("json", true);
    public static final FileExtension ser = new FileExtension("ser", true);

    private String ext;
    private boolean ignoreCase;

    public FileExtension(String ext, boolean ignoreCase) {
        this.ext = ext;
        this.ignoreCase = ignoreCase;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "FileExtension.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(BaseFile baseFile) {
        String name = baseFile.getName();
        String ext = FileUtil.getFileExtension(name);
        if (ignoreCase) {
            return this.ext.equalsIgnoreCase(ext);
        }
        return this.ext.equals(ext);
    }
}
