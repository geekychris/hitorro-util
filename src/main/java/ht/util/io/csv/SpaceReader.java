package ht.util.io.csv;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BaseFileUtil;
import ht.util.core.iterator.CloseableIterator;
import ht.util.core.string.StringUtil;
import ht.util.io.csv.csvconsumer.CSVConsumer;

/**
 * Dumb reader that takes lines like:
 * <p/>
 * chris       1.222  2.22 3
 * <p/>
 * and turns them into an array of {"chris", "1.222", "2.22", "3}
 */
public class SpaceReader {
    private CloseableIterator<String> iter;

    public SpaceReader(BaseFile inputFile) {
        iter = BaseFileUtil.bf2lineiter.apply(inputFile);
    }

    public void consume(CSVConsumer consumer) {
        int row = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            String parts[] = StringUtil.tokenizeFromSingleChar(s, " ", true);
            consumer.line(row++, parts);
        }
    }
}
