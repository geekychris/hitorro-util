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
package com.hitorro.util.urlhandlers;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.string.Fmt;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class PlatformDIRUrlStreamHandler extends URLStreamHandler {
    private URL m_url;
    private File m_root;

    public PlatformDIRUrlStreamHandler(File root) {
        m_root = root;
    }

    public static void register() {
        try {
            PlatformBinUrlStreamHandlerFactory factory = new PlatformBinUrlStreamHandlerFactory();
            URL.setURLStreamHandlerFactory(factory);
        } catch (Error e) {
            // Factory already set (e.g., by Spring Boot/Tomcat). This is acceptable in Spring Boot environments.
            // The custom protocols (htbin://, hthome://) may not work, but the application can still function.
            // Log at debug level to avoid alarming users in Spring Boot applications.
            System.err.println("Note: URL stream handler factory already registered. Custom URL protocols (htbin://, hthome://) may not be available.");
        }
    }

    public URLConnection openConnection(URL url) throws IOException {
        String host = url.getHost();
        String path = url.getPath();
        String u = Fmt
                .S("%s//%s//%s", m_root.getCanonicalPath(), host, path);
        URI newURI = new File(u).toURI();
        URL newURL = newURI.toURL();
        return newURL.openConnection();
    }
}

class PlatformBinUrlStreamHandlerFactory implements URLStreamHandlerFactory {
    public PlatformBinUrlStreamHandlerFactory() {
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("htbin")) {
            return new PlatformDIRUrlStreamHandler(Env.getBin());
        } else if (protocol.equals("hthome")) {
            return new PlatformDIRUrlStreamHandler(Env.getHome());
        }
        return null;
    }
}
