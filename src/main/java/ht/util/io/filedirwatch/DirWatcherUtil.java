package ht.util.io.filedirwatch;

import ht.util.core.Env;
import ht.util.core.Log;

/**
 * Watch a directory and Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time:
 * 10:10:06 AM
 */
public class DirWatcherUtil {
    public static final DirectoryWatch getWatcherFromParams() {
        DirectoryWatch delete = null;
        if (Env.getArchiveLogDir() == null) {
            Log.util.error("Unable to get the archive directory for this server");
            return null;
        }
        if (DirWatcherParams.DeleteEnabled.apply()) {
            String ext;
            if (DirWatcherParams.ZipEnabled.apply()) {
                ext = "gz";
            } else {
                ext = "log";
            }
            delete = new DirectoryWatch(Env.getArchiveLogDir(), ext,
                    DirWatcherParams.DeleteMaxFiles.apply(),
                    DirWatcherParams.DeleteMaxFileSize.apply(),
                    new DeleteTask(),
                    null,
                    false);
        }
        if (DirWatcherParams.ZipEnabled.apply()) {
            return new DirectoryWatch(Env.getArchiveLogDir(), "log",
                    DirWatcherParams.MaxZippedFiles.apply(),
                    DirWatcherParams.MaxGZSize.apply(),
                    new ZipTask(true),
                    delete,
                    false);
        } else {
            return delete;
        }
    }
}
