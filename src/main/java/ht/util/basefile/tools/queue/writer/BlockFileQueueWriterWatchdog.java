package ht.util.basefile.tools.queue.writer;

import ht.util.core.Log;
import ht.util.core.thread.TimerCallback;

import java.io.IOException;

/**
 * User: chris
 */
public class BlockFileQueueWriterWatchdog implements TimerCallback {
    private BlockFileQueueWriter writer;

    public BlockFileQueueWriterWatchdog(BlockFileQueueWriter p) {
        writer = p;
    }

    public boolean callback() {
        try {
            writer.flushIfMature();
        } catch (IOException e) {
            Log.filesystem.error("Unable to flush %s %e", e, e);
        }
        return true;
    }
}