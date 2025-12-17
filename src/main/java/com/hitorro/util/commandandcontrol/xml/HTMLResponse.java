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

import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.ListUtil;
import org.xml.sax.helpers.AttributesImpl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HTMLResponse extends com.hitorro.util.commandandcontrol.Response {
    private static final String Refresh = "<noscript>\n" +
            "<meta http-equiv=\"refresh\" content=\"2\">\n" +
            "</noscript> \n" +
            "<script language=\"JavaScript\">\n" +
            "<!--\n" +
            "var sURL = unescape(window.location.pathname);\n" +

            "function doLoad() { setTimeout( \"refresh()\", %s*1000 );}\n" +

            "function refresh() {  window.location.href = sURL; }\n" +
            "//-->\n" +
            "</script>\n" +

            "<script language=\"JavaScript1.1\">\n" +
            "<!--\n" +
            "function refresh() {   window.location.replace( sURL ); }\n" +
            "//-->\n" +
            "</script>\n" +

            "<script language=\"JavaScript1.2\">\n" +
            "<!-- \n" +
            "function refresh() { window.location.reload( false );}\n" +
            "//-->\n" +
            "</script>\n";
    private static AttributesImpl EmptyAttributes = new AttributesImpl();
    PrintWriter writer;
    String m_columnHeaders[];
    String transaction;
    List<String> infoList = new ArrayList<String>();
    List<String> warnList = new ArrayList();
    List<String> errorList = new ArrayList();
    List<String> debugList = new ArrayList();
    private boolean headerStarted = false;
    private boolean ended = false;

    //
    public HTMLResponse(String trans, OutputStream os, int refreshSecs) {
        transaction = trans;
        writer = new PrintWriter(os);

        writer.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        writer.print("<html lang=\"en\">");
        writer.print("<head>");
        writer.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">");
        if (refreshSecs > 0) {
            this.outputRefresh(refreshSecs);
            writer.print("<head>");
            writer.print("<body onload=\"doLoad()\">");
        } else {
            writer.print("<head>");
            writer.print("<body>");
        }

    }

    public void setResponseShape(com.hitorro.util.commandandcontrol.ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getHeaderLong());
    }

    public void addBannerRow(String row) {
        Console.print(writer, "<b>%s</b>", row);
    }

    public void addHeaderArray(String columnHeaders[]) {
        String t[] = getModifiedHeader(columnHeaders);

        m_columnHeaders = t;

        headerStarted = true;
        writer.print("<TABLE  border=\"2\">");
        writer.print("<TR>");
        int size = m_columnHeaders.length;
        for (int i = 0; i < size; i++) {
            Console.print(writer, "<TH>%s</TH>", m_columnHeaders[i]);
        }
        writer.print("</TR>");
    }

    private String[] getModifiedHeader(String columnHeaders[]) {
        List<String> result = new ArrayList();
        com.hitorro.util.commandandcontrol.GroupTuple gts[] = this.getShape().getGroups();
        for (int i = 0; i < columnHeaders.length; i++) {
            if (gts[i] != null) {
                result.add(gts[i].getSetName());
                i += gts[i].getSize() - 1;
                continue;
            }
            result.add(columnHeaders[i]);
        }
        return ArrayUtil.getStringArrayFromStringList(result);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
        containers = null;
    }

    public void addRowArray(Object elements[]) {
        write(elements, m_columnHeaders, shape.getRowName(), shape.getRowName());
        containers = null;
    }

    private void write(Object row[], String names[], String outerKey, String elementKey) {
        int size = row.length;
        writer.print("<tr>");
        for (int i = 0; i < size; i++) {
            Console.println(writer, com.hitorro.util.commandandcontrol.RenderingContainer.renderForHtml(this.containers, i, row));
        }
        writer.print("</tr>");
    }

    public void addError(String error) {
        errorList.add(error);
    }

    public void addWarning(String warning) {
        warnList.add(warning);
    }

    public void addInfo(String info) {
        infoList.add(info);
    }

    public void addDebug(String info) {
        debugList.add(info);
    }

    public void addInfo(com.hitorro.util.commandandcontrol.InfoLevel level, String info) {
        switch (level) {
            case Debug:
                addDebug(info);
                break;
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

    public void end() {
        if (ended) {
            return;
        }
        if (headerStarted) {
            writer.print("</TABLE>");
        }
        reportInfo("debug", debugList);
        reportInfo("info", infoList);
        reportInfo("warn", warnList);
        reportInfo("error", errorList);
        writer.print("</body></html>");
        writer.flush();
        writer.close();
    }

    private void reportInfo(String severity, List<String> msgs) {
        if (!ListUtil.nullOrEmpty(msgs)) {
            Console.println(writer, "<H2>%s</H2>", severity);
            for (String s : msgs) {
                Console.println(writer, "%s<b>");
            }
        }
    }

    public com.hitorro.util.commandandcontrol.MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new HTMLMultiRowResponse(shape.getHeaderShort().length, this, this.shape);
    }

    public void addMultiRowResponse(com.hitorro.util.commandandcontrol.MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }

    private void outputRefresh(int seconds) {
        Console.println(writer, Refresh, seconds);

    }
}

