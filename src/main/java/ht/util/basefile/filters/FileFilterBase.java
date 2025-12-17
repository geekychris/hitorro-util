package ht.util.basefile.filters;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalNotOperator;

/**
 *
 */
public abstract class FileFilterBase implements HTPredicate<BaseFile> {
    public static HTPredicate<BaseFile> not(HTPredicate<BaseFile> filt) {
        return new LogicalNotOperator(filt);
    }

    public static HTPredicate<BaseFile> isDir() {
        return IsDir.isDir;
    }

    public static HTPredicate<BaseFile> notDir() {
        return IsDir.notDir;
    }

    public static HTPredicate<BaseFile> hasExt(String ext) {
        return new FileExtension(ext, true);
    }

    public static HTPredicate<BaseFile> nameContains(String ext) {
        return new FileNameContains(ext, true);
    }

    public static HTPredicate<BaseFile> endsWith(String ends) {
        return new FileEndsWith(ends, true);
    }

    public HTPredicate<BaseFile> not() {
        return new LogicalNotOperator(this);
    }
}
