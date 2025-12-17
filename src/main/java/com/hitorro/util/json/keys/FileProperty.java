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
package com.hitorro.util.json.keys;


import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.mappers.JsonNodeToFile;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.io.File;

/**
 *
 */
public class FileProperty extends BaseMappingProperty<File> {
    public FileProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description,
                getDefaultFile(defaultValue),
                JsonNodeToFile.instance);
    }

    public FileProperty(String path, String description) {
        super(new Propaccess(path), description, null, JsonNodeToFile.instance);
    }

    public FileProperty(String path, String description, File defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description, File defaultValue) {
        super(path, description, defaultValue, JsonNodeToFile.instance);
    }

    public FileProperty(Propaccess path, String description) {
        super(path, description, null, JsonNodeToFile.instance);
    }

    private static File getDefaultFile(String defaultValue) {
        String resolved = JVSProperties.getProperties().resolveJsonVariable(defaultValue);
        if (StringUtil.nullOrEmptyString(resolved)) {
            return null;
        }
        return new File(resolved);
    }
}
