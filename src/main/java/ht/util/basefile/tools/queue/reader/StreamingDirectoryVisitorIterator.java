package ht.util.basefile.tools.queue.reader;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.queue.reader.serializer.WalkerDeserializer;
import ht.util.basefile.tools.queue.reader.serializer.WalkerDeserializerFactory;
import ht.util.core.Log;
import ht.util.core.Timer;
import ht.util.core.iterator.CloseableIterator;
import ht.util.typesystem.BaseSession;

import java.io.IOException;
import java.util.Iterator;

/**
 * User: chris
 */
public class StreamingDirectoryVisitorIterator<T> implements Iterator<T> {
    private DirectoryVisitorIterator dwi;
    private CloseableIterator<T> xsi;
    private T curr;
    private BaseFile currentFile = null;
    private boolean shouldNotify = false;
    private boolean initialRead = true;
    private EndFileNotification callback;
    private BaseSession baseSession;
    private WalkerDeserializerFactory factory;
    private Timer timer = new Timer();

    public StreamingDirectoryVisitorIterator(DirectoryVisitorIterator dwi, BaseSession session) {
        this.dwi = dwi;
        this.baseSession = session;
        this.factory = WalkerDeserializerFactory.getInstance();
    }

    public T next() {
        T res;

        if (curr == null) {
            // need to attempt to open the next file
            if (nextFile()) {
                if (!nextAux()) {
                    return null;
                }
            }
        }

        res = curr;
        nextAux();

        return res;
    }

    public void close() throws Exception {
        if (xsi != null) {
            xsi.close();
        }
    }

    public boolean hasNext() {
        initialCase();
        if (curr != null) {
            return true;
        }
        attemptNotify();
        return dwi.hasNext();
    }

    private void attemptNotify() {
        if (shouldNotify) {
            shouldNotify = false;
            if (callback != null) {
                timer.stop();
                callback.endOfBlock(currentFile, timer.getTime());
            }
        }
    }

    private final void initialCase() {
        if (initialRead) {
            nextFile();
            nextAux();
            initialRead = false;
        }
    }

    public void setEndOfCurrentBlock(EndFileNotification callback) {
        this.callback = callback;
        dwi.setEndOfBlockNotificationInterface(callback);
    }

    private boolean nextAux() {
        curr = null;
        if (xsi == null) {
            return false;
        }
        if (xsi.hasNext()) {
            shouldNotify = true;
            curr = xsi.next();
            return true;
        }
        return false;
    }

    private boolean nextFile() {
        do {
            if (!dwi.hasNext()) {
                return false;
            }
            currentFile = dwi.next();
            if (currentFile == null) {
                return false;
            }
        }
        while (!currentFile.exists());

        return moveOnGetNextFile();
    }

    private boolean moveOnGetNextFile() {
        WalkerDeserializer deserializer = factory.getDeserializer(currentFile.getExtension());
        Log.filesystem.info("Deserializing file: %s", currentFile);
        try {
            xsi = deserializer.getIterator(currentFile, baseSession);
            timer.reset();
            timer.start();
        } catch (IOException e) {
            Log.filesystem.error("Unable to get iterator for %s with error %s %e", currentFile, e, e);
            return false;
        }

        return true;
    }

    public void remove() {

    }
}
