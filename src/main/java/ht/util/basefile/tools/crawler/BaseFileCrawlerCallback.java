package ht.util.basefile.tools.crawler;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.sinks.Sink;

import java.io.IOException;

/**
 *
 */
public abstract class BaseFileCrawlerCallback implements Sink<BaseFile> {
    @Override
    public boolean stop() throws IOException {
        return false;
    }
}
