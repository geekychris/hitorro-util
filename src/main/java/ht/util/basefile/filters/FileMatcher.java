package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTAssert;

/**
 * User: chris
 */
public class FileMatcher extends FileFilterBase {
    private String name;
    private boolean caseSensative;

    public FileMatcher(String name, boolean caseSensative) {
        this.name = name;
        this.caseSensative = caseSensative;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "FileMatcher.initFromMap not implemented");
        return false;
    }

    public void initForPass() {
    }

    public boolean test(BaseFile af) {
        String n = af.getName();
        if (caseSensative) {
            return name.equals(n);
        } else {
            return name.equalsIgnoreCase(n);
        }
    }
}
