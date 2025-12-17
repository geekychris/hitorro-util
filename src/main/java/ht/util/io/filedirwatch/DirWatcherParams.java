package ht.util.io.filedirwatch;

import ht.util.core.Constants;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.IntegerProperty;
import ht.util.json.keys.LongFromBytesProperty;

public class DirWatcherParams {
    public static final String Watcher = "log.watcher";
    public static final BooleanProperty ZipEnabled = new BooleanProperty(Watcher + ".gz.enabled",
            "enable compression of log files",
            false);
    public static final LongFromBytesProperty MaxGZSize =
            new LongFromBytesProperty(Watcher + ".gz.maxsize",
                    "Max size in bytes of log files before, beyond which are zipped",
                    Constants.GBytes * 5);

    public static final IntegerProperty MaxZippedFiles =
            new IntegerProperty(Watcher + ".gz.maxfiles",
                    "Maximum number of log files, that beyond that which will be zipped ",
                    5);

    public static final BooleanProperty DeleteEnabled =
            new BooleanProperty(Watcher + ".delete.enabled",
                    "enable deleting of log files",
                    false);

    public static final LongFromBytesProperty DeleteMaxFileSize =
            new LongFromBytesProperty(Watcher + ".delete.maxsize",
                    "Max size in bytes of log files before, beyond which are deleted",
                    Constants.GBytes * 5);

    public static final IntegerProperty DeleteMaxFiles =
            new IntegerProperty(Watcher + ".delete.maxfiles",
                    "Max number of log files, that beyond that which will be deleted ",
                    20);


}

