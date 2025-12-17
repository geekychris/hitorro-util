package ht.util.io;

import java.io.File;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 15, 2004 Time: 4:46:45 AM Read a stream
 * of text and at word boundaries
 */
public class WordStream extends TextReader {
    public WordStream(String buffer) {
        super(buffer);
    }

    public WordStream(File file) {
        super(file);
    }
}
