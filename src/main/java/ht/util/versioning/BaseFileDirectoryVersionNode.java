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

import ht.util.basefile.fs.BaseFile;

import java.util.Map;

/**
 *
 */
public class BaseFileDirectoryVersionNode extends VersionNode {
    protected BaseFile m_directory;
    private Map<String, String> m_manifest;

    public BaseFileDirectoryVersionNode(Map<String, String> manifest, BaseFile directory, long major, long minor, long patch, long buildNumber) {
        super(major, minor, patch, buildNumber);
        m_directory = directory;
        m_manifest = manifest;
    }

    public BaseFileDirectoryVersionNode() {

    }

    public BaseFileDirectoryVersionNode(Map<String, String> manifest, BaseFile directory, String schemaVersion) {
        super(schemaVersion);
        m_directory = directory;
        m_manifest = manifest;
    }

    public BaseFile getDirectory() {
        return m_directory;
    }

    public void setDirectory(BaseFile dir) {
        m_directory = dir;
    }
}
