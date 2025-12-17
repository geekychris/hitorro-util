/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.io;

import java.io.InputStream;

/**
 * This class provides a cover over an IO stream which guarantees that the stream can be restarted
 */
public interface RestartableInput {
    /**
     * Set the stream back to its beginning. After calling restart you will need to call getStream again.
     */
    void restart();

    /**
     * Get the Input stream. We do not guarantee that the same input stream is returned after restart.
     *
     * @return the input stream or null if there is a problem
     */
    InputStream getStream();
}

