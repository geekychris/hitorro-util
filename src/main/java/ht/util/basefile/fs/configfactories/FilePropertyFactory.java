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
package ht.util.basefile.fs.configfactories;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.file.FileFile;
import ht.util.basefile.fs.file.FileFileSystem;
import ht.util.json.keys.StringProperty;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class FilePropertyFactory extends BaseFilePropertyFactory<FileConfig, FileFile> {
    private static final String FS = "file";
    public static StringProperty PathKey = new StringProperty("path", "path to file root", null);

    public String[] getNames() {
        return new String[]{"fileconfig"};
    }

    public FileConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        FileConfig dfs = new FileConfig();
        dfs.path = PathKey.apply(map);
        return dfs;
    }

    public String getProtocol() {
        return FS;
    }

    /**
     * Overide for file system as we dont want the fancy
     *
     * @param val
     * @return
     */
    public BaseFile getBaseFileFromPath(String val) throws IOException {
        val = JVSProperties.getProperties().resolveJsonVariable(val);

        BaseFile ff = super.getBaseFileFromPath(val);
        if (ff != null) {
            // we were using some kind of config in the path.
            return ff;
        }
        // we are just a basic file!
        File f = new File(val.substring(FS.length() + 1));
        FileFileSystem prov = new FileFileSystem(f);
        return prov.getFile("");
    }

    @Override
    public FileSystemConfig getConfigFromParts(final String[] parts) {
        return new FileConfig();
    }
}