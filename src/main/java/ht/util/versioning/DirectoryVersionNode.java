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
package ht.util.versioning;

import ht.jsontypesystem.JVS;

import java.io.File;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 4:08:19 PM
 */
public class DirectoryVersionNode extends VersionNode {
    protected File m_directory;
    private JVS m_manifest;

    public DirectoryVersionNode(JVS manifest, File directory, long major, long minor, long patch, long buildNumber) {
        super(major, minor, patch, buildNumber);
        m_directory = directory;
        m_manifest = manifest;
    }

    public DirectoryVersionNode() {

    }

    public DirectoryVersionNode(JVS manifest, File directory, String schemaVersion) {
        super(schemaVersion);
        m_directory = directory;
        m_manifest = manifest;
    }

    public File getDirectory() {
        return m_directory;
    }

    public void setDirectory(File dir) {
        m_directory = dir;
    }
}
