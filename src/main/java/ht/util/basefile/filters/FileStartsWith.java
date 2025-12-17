package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTAssert;
import ht.util.core.string.StringUtil;

/**
 *
 */
public class FileStartsWith extends FileFilterBase {
    private String startsWith;
    private boolean ignoreCase;

    public FileStartsWith(String startsWith, boolean ignoreCase) {
        this.startsWith = startsWith;
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
        return StringUtil.startsWithIgnoreCase(startsWith, name, ignoreCase);
    }
}
