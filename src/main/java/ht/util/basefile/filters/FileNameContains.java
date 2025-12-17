package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTAssert;

/**
 *
 */
public class FileNameContains extends FileFilterBase {
    private String str;
    private boolean ignoreCase;

    public FileNameContains(String str, boolean ignoreCase) {
        this.str = str;
        this.ignoreCase = ignoreCase;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "FileNameContains.initFromMap not implemented");
        return false;
    }

    public void initForPass() {
    }

    public boolean test(BaseFile af) {
        String n = af.getName();
        if (ignoreCase) {
            n = n.toLowerCase();
        }
        return n.contains(str);
    }
}

