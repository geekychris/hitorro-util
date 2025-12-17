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
package com.hitorro.util.basefile.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;

/**
 *
 */
public class FileStartsEndsWith extends FileFilterBase {
    private String matchMe;
    private boolean ignoreCase;
    private boolean endsWith;

    /**
     * @param matchMe    string to test
     * @param ignoreCase true if we wish to ignore case
     * @param endsWith   false if we wish to test the start of the name true for the end of the name
     */
    public FileStartsEndsWith(String matchMe, boolean ignoreCase, boolean endsWith) {
        this.endsWith = endsWith;
        if (ignoreCase) {
            this.matchMe = matchMe.toLowerCase();
        } else {
            this.matchMe = matchMe;
        }
        this.ignoreCase = ignoreCase;

    }

    public boolean initFromMap(final JsonNode map) {
        return false;
    }

    @Override
    public void initForPass() {
    }

    public boolean test(BaseFile file) {
        String name = file.getName();
        if (ignoreCase) {
            name = name.toLowerCase();
        }
        if (endsWith) {
            return name.endsWith(matchMe);
        } else {
            return name.startsWith(matchMe);
        }
    }
}