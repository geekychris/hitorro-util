package ht.util.basefile.tools.crawler;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.iterator.sinks.Sink;
import ht.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 * Enumerator of a nested directory structure.  You provide the root of the directory and a file constraint to ensure
 * that it only looks at directories you want and files that you want.
 */
public class DirectoryEnumerator implements Runnable {
    private BaseFileCrawler crawler;
    private int pauseSeconds;
    private Object notifyMe = new Object();

    public DirectoryEnumerator(BaseFile root, Sink<BaseFile> callback, HTPredicate<BaseFile> fileConstraint, int pauseSeconds) {
        crawler = new BaseFileCrawler();
        crawler.setCallback(callback);
        crawler.setOperator(fileConstraint);
        crawler.setRoot(root);
        this.pauseSeconds = pauseSeconds;
    }

    public void run() {
        while (true) {
            try {
                enumerate();
                Env.sleepNSeconds(pauseSeconds, notifyMe);
            } catch (IOException e) {
                Log.filesystem.error("Unable to access directory for enumeration", e, e);
            }
        }
    }

    public void enumerate() throws IOException {
        crawler.execute();
    }

    public void notifyChangeToEnumerate() {
        synchronized (notifyMe) {
            notifyMe.notifyAll();
        }
    }
}