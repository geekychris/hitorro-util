package ht.util.core.iterator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 1:43:34 PM
 * <p/>
 * Recurse a filepath in an efficient way (this is really a cursor over a nested directory
 */
public class FilePathIterator extends AbstractIterator<String> {
    private List<String> m_files = new ArrayList<String>();
    private int m_removeLeft;
    private String path;

    public FilePathIterator(String path, boolean includeJars) {
        m_removeLeft = path.length();
        recursiveList(path);
        this.path = path;
    }

    private void recursiveList(String path) {
        File f = new File(path);
        File files[] = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                recursiveList(file.getAbsolutePath());
            } else {
                String v = file.getAbsolutePath().substring(m_removeLeft + 1);
                m_files.add(v);
            }
        }
    }

    public boolean hasNext() {
        return m_files.size() > 0;
    }

    public String next() {
        String returnMe = m_files.get(m_files.size() - 1);
        m_files.remove(m_files.size() - 1);
        return returnMe;
    }

    public void remove() {
    }

    @Override
    public void close() throws Exception {
    }
}
