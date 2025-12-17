package ht.util.basefile.tools.crawler;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.sinks.Sink;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.core.opers.HTPredicate;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Recursively traverse a directory structure passing
 * any matching files to the callback
 */
public class BaseFileCrawler {
    private BaseFile root;
    private Sink<BaseFile> callback;
    private HTPredicate<BaseFile> oper = new AlwaysTrueOperator();

    public void setOperator(HTPredicate<BaseFile> oper) {
        this.oper = oper;
    }

    public void setRoot(BaseFile root) {
        this.root = root;
    }

    public void setCallback(Sink<BaseFile> callback) {
        this.callback = callback;
    }

    public int execute() throws IOException {
        callback.start();
        int cnt = 0;
        try {
            cnt = executePrivate(root);
        } catch (StoreException e) {
            throw new IOException(e);
        }
        callback.stop();
        return cnt;
    }

    private int executePrivate(BaseFile dir) throws IOException, StoreException {
        int count = 0;
        BaseFile bfs[] = dir.listFiles(oper);
        for (BaseFile bf : bfs) {
            if (bf.isDir()) {
                count += executePrivate(bf);
            } else {
                callback.add(bf);
                count++;
            }
        }
        return count;
    }
}
