package ht.util.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (c) 2003-2008 HiTorro All rights reserved. User: chris Date: Oct 27, 2006 Time: 11:29:10 AM
 * <p/>
 * Mail Attachment
 * <p/>
 * Note: getCInputStream() creates a new instance of an CInputStream
 */
public class Attachment {

    private byte _bytes[];
    private String _name;

    public Attachment(String name, byte bytes[]) {
        _name = name;
        _bytes = bytes;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(_bytes);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream();
    }

}
