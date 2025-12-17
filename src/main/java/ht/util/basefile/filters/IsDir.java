package ht.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.HTAssert;
import ht.util.core.opers.LogicalNotOperator;

/**
 * User: chris
 */
public class IsDir extends FileFilterBase {
    public static IsDir isDir = new IsDir();
    public static LogicalNotOperator notDir = new LogicalNotOperator(IsDir.isDir);

    public void initForPass() {

    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "IsDir.initFromMap not implemented");
        return false;
    }

    public boolean test(BaseFile baseFile) {
        return baseFile.isDir();
    }
}
