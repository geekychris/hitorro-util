package ht.util.io.largedata.compressedstreams;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 21, 2004 Time: 10:51:54 AM
 * <p/>
 * Description:
 * <p/>
 * Exctends the concept of the compressed output stream so that we can we can acknowledge the starting offset of the
 * block
 */
public class BlockRegisteredOutputStream extends FSOutputStream {
    protected long m_blockStart = 0;

    public BlockRegisteredOutputStream(File path)
            throws IOException {
        super(path);
    }

    public long markStartOfExpression() {
        m_blockStart = getCurrentFileOffset();
        return m_blockStart;
    }

    public void markEndOfExpression()
            throws IOException {
        flush();
    }
}