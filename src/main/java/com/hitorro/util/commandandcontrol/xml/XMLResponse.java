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
package com.hitorro.util.commandandcontrol.xml;

import com.megginson.sax.DataWriter;
import com.hitorro.util.commandandcontrol.InfoLevel;
import com.hitorro.util.commandandcontrol.MultiRowResponse;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;


public class XMLResponse extends Response {
    public static final String BannerKey = "Banner";
    public static final String ErrorKey = "Error";
    public static final String WarningKey = "Warning";
    public static final String InfoKey = "Info";
    private static AttributesImpl Empty = new AttributesImpl();
    protected DataWriter writer;
    protected String headers[];
    protected String transaction;
    protected boolean ended = false;

    public XMLResponse(String trans, OutputStream os) throws SAXException {
        transaction = trans;
        writer = new DataWriter();
        PrintWriter writer;

        writer = new PrintWriter(os);
        this.writer.setIndentStep(2);
        this.writer.setOutput(writer);
        try {
            this.writer.startDocument();
        } catch (SAXException e) {
            Log.commands.error("failure writing to output stream %s %e", e, e);
        }
        AttributesImpl attributes = new AttributesImpl();
        this.writer.startElement("", transaction, "", attributes);
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getShortNames());
    }

    public void addBannerRow(String row) {
        AttributesImpl attributes = new AttributesImpl();
        try {
            writer.startElement("", BannerKey, "", attributes);
            writer.characters(row);
            writer.endElement("", BannerKey, "");
        } catch (SAXException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addHeaderArray(String columnHeaders[]) {
        headers = columnHeaders;
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
        containers = null;
    }

    public void addRowArray(Object elements[]) {
        write(elements, headers, shape.getRowName(), shape.getRowName());
    }

    private void write(Object row[], String names[], String outerKey, String elementKey) {
        AttributesImpl attributes = new AttributesImpl();
        try {
            writer.startElement("", outerKey, "", attributes);
            int size = row.length;
            for (int i = 0; i < size; i++) {
                Object o = row[i];

                writer.startElement("", names[i], "", Empty);
                if (o != null) {
                    writer.characters(o.toString());
                }

                writer.endElement("", names[i], "");
            }
            writer.endElement("", outerKey, "");
        } catch (SAXException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addError(String error) {
        AttributesImpl attributes = new AttributesImpl();
        try {
            writer.startElement("", ErrorKey, "", attributes);
            writer.characters(error);
            writer.endElement("", ErrorKey, "");
        } catch (SAXException e) {
            Log.util.error("%s %e", e, e);
        }

    }

    public void addWarning(String warning) {
        AttributesImpl attributes = new AttributesImpl();
        try {
            writer.startElement("", WarningKey, "", attributes);
            writer.characters(warning);
            writer.endElement("", WarningKey, "");
        } catch (SAXException e) {
            Log.util.error("%s %e", e, e);
        }

    }


    public void addInfo(String info) {
        AttributesImpl attributes = new AttributesImpl();
        try {
            writer.startElement("", InfoKey, "", attributes);
            writer.characters(info);
            writer.endElement("", InfoKey, "");
        } catch (SAXException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addInfo(InfoLevel level, String info) {
        switch (level) {
            case Info:
                addInfo(info);
                break;
            case Warn:
                addWarning(info);
                break;
            case Error:
                addError(info);
                break;
        }
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
    }

    public void end() {
        if (ended) {
            return;
        }
        try {
            ended = true;
            writer.endElement("", transaction, "");
            writer.flush();
        } catch (SAXException e) {
            Log.commands.error("%s %e", e, e);
        } catch (IOException e) {
            Log.commands.error("%s %e", e, e);
        }
    }

    public MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new XMLMultiRowResponse(shape.getHeaderShort().length, this, this.shape);
    }

    public void addMultiRowResponse(MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }
}

