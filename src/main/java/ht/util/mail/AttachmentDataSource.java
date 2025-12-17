package ht.util.mail;

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
