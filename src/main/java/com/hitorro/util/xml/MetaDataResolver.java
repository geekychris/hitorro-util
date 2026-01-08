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
package com.hitorro.util.xml;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.string.Fmt;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.File;
import java.net.URI;

/**
 */

public class MetaDataResolver implements EntityResolver {
    public MetaDataResolver() {

    }

    private static File getPath(String file) {
        return new File(Fmt.S("%s/data/dtds/%s", Env.getBin().getAbsolutePath(), file));
    }

    public InputSource resolveEntity(String publicId,
                                     String systemId) {
        if (systemId != null) {
            String name = null;
            int slashIdx = systemId.lastIndexOf("/");

            if (slashIdx == -1) {
                name = systemId;
            } else {
                name = systemId.substring(slashIdx + 1);
            }
            File file = getPath(name);
            URI url = file.toURI();
            if (url != null) {
                String urlString = url.toASCIIString();
                // Put some logging here
                return new InputSource(urlString);
            }
        }
        return null;
    }
}

