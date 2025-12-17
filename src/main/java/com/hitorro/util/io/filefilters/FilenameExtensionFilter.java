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
package com.hitorro.util.io.filefilters;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.io.FilenameFilter;


/**
 * HTPredicate that looks for files with a similar extension. Can compare the extensions ignoring the case if you so wish.
 *
 * @author ccollins
 */
public class FilenameExtensionFilter implements FilenameFilter {

    public static final FilenameExtensionFilter Jar = new FilenameExtensionFilter("jar", true);
    public static final FilenameExtensionFilter Zip = new FilenameExtensionFilter("zip", true);
    public static final FilenameExtensionFilter Sh = new FilenameExtensionFilter("sh", true);

    public static final OrCollection JarOrZip = new OrCollection(Jar, Zip);

    private String m_extension;
    private boolean m_ignoreCase;

    /**
     * Constructor.
     *
     * @param extension  to look for
     * @param ignoreCase if true do a case insensative search
     */
    public FilenameExtensionFilter(String extension, boolean ignoreCase) {
        if (ignoreCase) {
            m_extension = extension.toLowerCase();
        } else {
            m_extension = extension;
        }
        m_ignoreCase = ignoreCase;

    }

    public boolean accept(File dir, String name) {
        String ext = FileUtil.getFileExtension(name);
        if (StringUtil.nullOrEmptyOrBlankString(ext)) {
            // not an interesting extension.
            return false;
        }
        if (m_ignoreCase) {
            ext = ext.toLowerCase();
        }
        return m_extension.equals(ext);
    }
}
