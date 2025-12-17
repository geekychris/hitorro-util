package ht.util.urlhandlers;

import ht.util.core.Env;
import ht.util.core.string.Fmt;

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
        PlatformBinUrlStreamHandlerFactory factory = new PlatformBinUrlStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(factory);
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
