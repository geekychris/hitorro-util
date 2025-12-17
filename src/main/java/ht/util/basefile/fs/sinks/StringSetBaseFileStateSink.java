package ht.util.basefile.fs.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BaseFileUtil;
import ht.util.json.keys.BasefileProperty;
import ht.util.json.keys.BooleanProperty;

import java.io.IOException;

/**
 * Receiver of a single string that we put into a hashtable where we ultimate write out the string along with its
 * frequency.
 */
public class StringSetBaseFileStateSink extends HashCountingBaseFileStateSink<String> {
    public static final BasefileProperty BaseFileKey = new BasefileProperty("outfile", "outputfile in the format of a basefile");
    public static BooleanProperty WriteCount = new BooleanProperty("writecount", "write the frequency count", false);
    private boolean writeCounts = false;

    public StringSetBaseFileStateSink(BaseFile outputFile, boolean writeCounts) {
        super(outputFile);
        this.writeCounts = writeCounts;
    }

    public StringSetBaseFileStateSink() {
        // called from configs
        super(null);
    }

    public boolean init(final JsonNode map) {
        writeCounts = WriteCount.apply(map);
        this.setBaseFile(BaseFileKey.apply(map));
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        if (writeCounts) {
            return BaseFileUtil.writeStringStringCountFromTObjectString(outputFile, set);
        } else {
            return BaseFileUtil.writeStringFromTObjectString(outputFile, set);
        }
    }
}
