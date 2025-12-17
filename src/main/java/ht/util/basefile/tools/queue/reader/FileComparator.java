package ht.util.basefile.tools.queue.reader;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.string.StringUtil;

import java.util.Comparator;

/**
 *
 */
public class FileComparator implements Comparator<BaseFile> {
    public int compare(BaseFile o1, BaseFile o2) {
        long o1l = StringUtil.getLongNumberFromText(o1.getName());

        long o2l = StringUtil.getLongNumberFromText(o2.getName());
        if (o1l > o2l) {
            return 1;
        } else {
            if (o1l < o2l) {
                return -1;
            }
        }
        return 0;

    }
}