package ht.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.FilePathIterator;
import ht.util.core.iterator.JarFileIterator;
import ht.util.core.opers.HTPredicate;
import ht.util.core.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 1:42:51 PM
 */
public class ClassPathDeepIterator extends AbstractIterator<String> {
    private boolean m_includeJars = false;
    private List<String> queue = new ArrayList<String>();
    private Iterator<String> currIterator = null;

    public ClassPathDeepIterator() {
        String classPath = System.getProperty("java.class.path");
        String toks[] = StringUtil.tokenizeFromSingleChar(classPath, File.pathSeparator);
        for (String tok : toks) {
            queue.add(tok);
        }
        currIterator = getIterator();
    }

    private Iterator<String> getIterator() {
        while (queue.size() > 0) {
            String path = queue.get(queue.size() - 1);
            queue.remove(queue.size() - 1);
            if (!path.endsWith("jar") || path.endsWith("zip")) {
                // we only examine non jar files
                return new FilePathIterator(path, m_includeJars);
            } else {
                return new JarFileIterator(new File(path)).filter(ClassLO.clo);
            }
        }
        return null;
    }

    public boolean hasNext() {
        while (currIterator != null) {
            if (currIterator.hasNext()) {
                return true;
            }
            currIterator = getIterator();
        }
        return false;
    }

    public String next() {
        if (currIterator != null) {
            return currIterator.next();
        }
        return null;
    }

    public void remove() {
    }
}

class ClassLO implements HTPredicate<String> {
    public static ClassLO clo = new ClassLO();

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final String s) {
        return StringUtil.endsWithIgnoringCase(s, ".class");
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }
}
