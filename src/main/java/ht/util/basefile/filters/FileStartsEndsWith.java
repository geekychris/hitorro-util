package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;

/**
 *
 */
public class FileStartsEndsWith extends FileFilterBase {
    private String matchMe;
    private boolean ignoreCase;
    private boolean endsWith;

    /**
     * @param matchMe    string to test
     * @param ignoreCase true if we wish to ignore case
     * @param endsWith   false if we wish to test the start of the name true for the end of the name
     */
    public FileStartsEndsWith(String matchMe, boolean ignoreCase, boolean endsWith) {
        this.endsWith = endsWith;
        if (ignoreCase) {
            this.matchMe = matchMe.toLowerCase();
        } else {
            this.matchMe = matchMe;
        }
        this.ignoreCase = ignoreCase;

    }

    public boolean initFromMap(final JsonNode map) {
        return false;
    }

    @Override
    public void initForPass() {
    }

    public boolean test(BaseFile file) {
        String name = file.getName();
        if (ignoreCase) {
            name = name.toLowerCase();
        }
        if (endsWith) {
            return name.endsWith(matchMe);
        } else {
            return name.startsWith(matchMe);
        }
    }
}