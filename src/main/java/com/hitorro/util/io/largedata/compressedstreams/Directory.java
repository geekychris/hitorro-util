/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.io.largedata.compressedstreams;

/**
 * Copyright 2004 The Apache Software Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

/**
 * A Directory is a flat listFiles of files.  Files may be written once, when they are created.  Once a file is created it
 * may only be opened for read, or deleted.  Random access is permitted both when reading and writing.
 * <p/>
 * <p> Java's i/o APIs not used directly, but rather all i/o is through this API.  This permits things such as: <ul>
 * <li> implementation of RAM-based indices; <li> implementation indices stored in a database, via JDBC; <li>
 * implementation of an index as a single file; </ul>
 *
 * @author Doug Cutting
 */
public abstract class Directory {
    /**
     * Returns an array of strings, one for each file in the directory.
     */
    public abstract String[] list()
            throws IOException;

    /**
     * Returns true iff a file with the given name exists.
     */
    public abstract boolean fileExists(String name)
            throws IOException;

    /**
     * Returns the time the named file was last modified.
     */
    public abstract long fileModified(String name)
            throws IOException;

    /**
     * Set the modified time of an existing file to now.
     */
    public abstract void touchFile(String name)
            throws IOException;

    /**
     * Removes an existing file in the directory.
     */
    public abstract void deleteFile(String name)
            throws IOException;

    /**
     * Renames an existing file in the directory. If a file already exists with the new name, then it is replaced. This
     * replacement should be atomic.
     */
    public abstract void renameFile(String from, String to)
            throws IOException;

    /**
     * Returns the length of a file in the directory.
     */
    public abstract long fileLength(String name)
            throws IOException;

    /**
     * Creates a new, empty file in the directory with the given name. Returns a stream writing this file.
     */
    public abstract COutputStream createFile(String name)
            throws IOException;

    /**
     * Returns a stream reading an existing file.
     */
    public abstract CInputStream openFile(String name)
            throws IOException;

    /**
     * Construct a {@link Lock}.
     *
     * @param name the name of the lock file
     */
    public abstract Lock makeLock(String name);

    /**
     * Closes the store.
     */
    public abstract void close()
            throws IOException;
}