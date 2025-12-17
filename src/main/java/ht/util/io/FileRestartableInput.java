/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.io;

import ht.util.core.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class provides a restartable input stream for files.
 */
public class FileRestartableInput implements RestartableInput {
    private File _file;
    private FileInputStream _stream;

    public FileRestartableInput(String filepath) {
        this(new File(filepath));
    }

    public FileRestartableInput(File ff) {
        _file = ff;
        _stream = null;
    }

    public void restart() {
        // we restart by recreating the stream
        _stream = null;
    }

    public InputStream getStream() {
        if (_stream == null) {
            try {
                _stream = new FileInputStream(_file);
            } catch (FileNotFoundException fnfe) {
                Log.util.error("getStream fileNotFound %s", fnfe);
                _stream = null;
            }
        }
        return _stream;
    }

}
