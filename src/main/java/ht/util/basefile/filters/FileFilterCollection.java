package ht.util.basefile.filters;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalAndOperator;
import ht.util.core.opers.LogicalOrOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to construct an AND or OR file filter set.
 * <p>
 * Simply construct and then chain calls together
 */
public class FileFilterCollection {
    private List<HTPredicate<BaseFile>> col = new ArrayList();

    public HTPredicate<BaseFile> and() {
        if (col.size() == 0) {
            return null;
        }
        if (col.size() == 1) {
            return col.get(0);
        }
        LogicalAndOperator lao = new LogicalAndOperator();
        lao.add(col);
        return lao;
    }

    public HTPredicate<BaseFile> or() {
        if (col.size() == 0) {
            return null;
        }
        if (col.size() == 1) {
            return col.get(0);
        }
        LogicalOrOperator lao = new LogicalOrOperator();
        lao.add(col);
        return lao;
    }

    public FileFilterCollection isDir() {
        col.add(IsDir.isDir);
        return this;
    }

    public FileFilterCollection notDir() {
        col.add(IsDir.notDir);
        return this;
    }

    public FileFilterCollection hasExt(String ext) {
        col.add(new FileExtension(ext, true));
        return this;
    }

    public FileFilterCollection notExt(String ext) {
        col.add(new FileExtension(ext, true).not());
        return this;
    }

    public FileFilterCollection nameContains(String ext) {
        col.add(new FileNameContains(ext, true));
        return this;
    }

    public FileFilterCollection notContains(String ext) {
        col.add(new FileNameContains(ext, true).not());
        return this;
    }

    public FileFilterCollection endsWith(String ends) {
        col.add(new FileEndsWith(ends, true));
        return this;
    }

    public FileFilterCollection notEndWith(String ends) {
        col.add(new FileEndsWith(ends, true).not());
        return this;
    }
}
