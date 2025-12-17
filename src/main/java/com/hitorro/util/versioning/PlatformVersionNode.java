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
package com.hitorro.util.versioning;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 6:36:11 PM
 * <p/>
 * Definition of a platform binary release and a listFiles of the server configurations that use it.
 */
public class PlatformVersionNode<T extends ConfigTag> extends DirectoryVersionNode {
    private int m_usageCount = 0;
    private List<T> m_serverConfigs = new ArrayList<T>();

    public PlatformVersionNode(JVS manifest,
                               File directory) throws PropaccessError {
        super(manifest, directory,
                manifest.getLong("major"),
                manifest.getLong("minor"),
                manifest.getLong("patch"),
                manifest.getLong("number"));
    }

    public PlatformVersionNode(File directory) {
        super(new JVS(), directory, 99, 99, 99, 99);
    }

    public void addDependentServer(T sc) {
        m_serverConfigs.add(sc);
    }

    public List<T> getConfigs() {
        return m_serverConfigs;
    }

    public void removeConfig(T sc) {
        m_serverConfigs.remove(sc);
    }

    public int getUsageCount() {
        return m_usageCount;
    }

    public boolean delete() {
        int count = m_serverConfigs.size();
        if (count > 0) {
            Log.util.error("Attempt to delete platform config %s failed as it is used by %s servers", this.getVersion(), count);
            return false;
        }
        FileUtil.deleteDirectoryContent(m_directory, true);
        return true;
    }
}
