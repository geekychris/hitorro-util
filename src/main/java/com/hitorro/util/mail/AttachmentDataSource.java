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
package com.hitorro.util.mail;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (c) 2003-2008 HiTorro All rights reserved. User: chris Date: Oct 27, 2006 Time: 11:51:02 AM
 */
public class AttachmentDataSource implements DataSource {

    private Attachment _attachment;

    public AttachmentDataSource(Attachment attachment) {
        _attachment = attachment;
    }

    /**
     * @return Content type of Attachment (currently: text/plain only)
     */
    public String getContentType() {
        return "text/plain";
    }

    /**
     * @return new instance of CInputStream to the attachment content
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        return _attachment.getInputStream();
    }

    public String getName() {
        return _attachment.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return _attachment.getOutputStream();
    }
}
