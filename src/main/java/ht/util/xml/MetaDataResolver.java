package ht.util.xml;

import ht.util.core.Env;
import ht.util.core.string.Fmt;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.File;
import java.net.URI;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Apr 16, 2004 Time: 5:56:00 PM
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

