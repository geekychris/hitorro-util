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
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.testframework.FilteredTestSuiteGenerator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class XMLJUnitResponse extends Response {

    private final String XmlEleTestSuite = "testsuite";
    private final String XmlAttrErrors = "errors";
    private final String XmlAttrTests = "tests";
    private final String XmlAttrFailures = "failures";
    private final String XmlAttrTime = "time";
    private final String XmlAttrRunLevel = "runlevel";
    private final String XmlEleTestCase = "testcase";
    private final String XmlAttrClassName = "classname";
    private final String XmlAttrName = "name";
    private final String XmlAttrEmail = "email";
    private final String XmlAttrDescription = "description";
    private final String XmlAttrStatus = "status";
    private final String XmlEleFailure = "failure";
    private final String XmlEleWarning = "warning";
    private final String XmlEleInfo = "info";
    private final String XmlAttrMessage = "message";
    private final String XmlAttrType = "type";
    private final int RowAttrName = 0;
    private final int RowAttrEmail = 1;
    private final int RowAttrClassName = 2;
    private final int RowAttrStatus = 3;
    private final int RowAttrDescription = 4;
    private final int RowAttrTime = 5;
    private DataWriter m_writer;
    private File m_File;
    private List<XMLJUnitResponseRow> responseRows = new ArrayList<XMLJUnitResponseRow>();
    private XMLJUnitResponseMsg responseMessage = null;
    private int m_errors = 0;
    private int m_failures = 0;
    private float m_time = 0.0F;


    public XMLJUnitResponse(File file) {
        this.m_File = file;
    }


    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getShortNames());
    }


    public void addBannerRow(String row) {
    }


    public void addHeader(String... columnHeaders) {
        addHeaderArray(columnHeaders);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }


    public void addHeaderArray(String columnHeaders[]) {
        m_writer = new DataWriter();


        File output = FileUtil.getDatedFileFromPattern(Env.getLogDir(), "junit_testsuite", "xml");
        PrintWriter writer;

        try {
            writer = FileUtil.getBufferedPrintWriterFromFile(output, "UTF-8");
            m_writer.setIndentStep(2);
            m_writer.setOutput(writer);
            try {
                m_writer.startDocument();
            } catch (SAXException e) {
                Log.test.error("failure writing to junit output file %s %s %e", output.toString(), e, e);
            }
        } catch (UnsupportedEncodingException e) {
            Log.test.error("failure opening junit output file %s %s %e", output.toString(), e, e);
        }


    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }


    public void addRowArray(Object elements[]) {
        /**
         *    create testcase row.  if there is a corresponding error/warning/info message,
         *    it already exists and is waiting to be picked up.  pick it up.  clear the XMLJUnitResponseMsg
         *    thereafter in time for the next row.
         */
        XMLJUnitResponseRow row;
        row = new XMLJUnitResponseRow(elements, responseMessage);
        responseRows.add(row);
        responseMessage = null;

        /*   extract, aggregate test case execution time.   */
        String rowTime = elements[RowAttrTime].toString();
        m_time = m_time + Float.valueOf(rowTime);

    }

    public void addInfo(InfoLevel level, String info) {
        switch (level) {
            case Info:
                responseMessage = new XMLJUnitResponseMsg(XMLJUnitResponseMsg.Type.info, info);
                break;
            case Warn:
                responseMessage = new XMLJUnitResponseMsg(XMLJUnitResponseMsg.Type.warning, info);
                break;
            case Error:
                responseMessage = new XMLJUnitResponseMsg(XMLJUnitResponseMsg.Type.error, info);

                /*   junitlistener caller has compressed errors and failures into one 'addError' call.  explode this.  */
                if (info.startsWith("Error ")) {
                    m_errors++;
                } else if (info.startsWith("Failure ")) {
                    m_failures++;
                }
                break;
        }
    }


    public void end() {
        AttributesImpl attributes = new AttributesImpl();

        try {
            /*   test suite   */
            String suiteName = FilteredTestSuiteGenerator.class.getName();
            String runLevel = FilteredTestSuiteGenerator.getSuiteRunLevel().toString();
            attributes.clear();
            attributes.addAttribute("", XmlAttrErrors, "", "", Integer.toString(m_errors));
            attributes.addAttribute("", XmlAttrFailures, "", "", Integer.toString(m_failures));
            attributes.addAttribute("", XmlAttrName, "", "", suiteName);
            attributes.addAttribute("", XmlAttrRunLevel, "", "", runLevel);
            attributes.addAttribute("", XmlAttrTests, "", "", Integer.toString(responseRows.size()));
            attributes.addAttribute("", XmlAttrTime, "", "", Float.toString(m_time));

            m_writer.startElement("", XmlEleTestSuite, "", attributes);

            /*  test cases: iterate   */
            for (XMLJUnitResponseRow responseRow : responseRows) {
                /*   test case: attributes   */
                String className = StringUtil.substring(responseRow.getAttribute(RowAttrClassName), "class ", "", 0);

                attributes.clear();
                attributes.addAttribute("", XmlAttrClassName, "", "", className);
                attributes.addAttribute("", XmlAttrName, "", "", responseRow.getAttribute(RowAttrName));
                attributes.addAttribute("", XmlAttrEmail, "", "", responseRow.getAttribute(RowAttrEmail));
                attributes.addAttribute("", XmlAttrTime, "", "", responseRow.getAttribute(RowAttrTime));
                attributes.addAttribute("", XmlAttrStatus, "", "", responseRow.getAttribute(RowAttrStatus));
                attributes.addAttribute("", XmlAttrDescription, "", "", responseRow.getAttribute(RowAttrDescription));

                m_writer.startElement("", XmlEleTestCase, "", attributes);

                /*  test case child element: failures/errors or warnings or info.  */
                String xmlElement = "";
                String rowStatus = responseRow.getAttribute(RowAttrStatus);
                String rowMessage = "";
                XMLJUnitResponseMsg.Type rowMessageType = XMLJUnitResponseMsg.Type.info;


                if ((responseRow.getMessage() != null)) {
                    rowMessageType = responseRow.getMessage().getType();

                    if (responseRow.getMessage().getMessage() != null) {
                        rowMessage = responseRow.getMessage().getMessage();
                    }
                }

                attributes.clear();

                /*   put child element.   */
                switch (rowMessageType) {
                    case error:
                        String failureType = StringUtil.substring(rowMessage, "with exception ", " ", 0);
                        xmlElement = XmlEleFailure;
                        attributes.addAttribute("", XmlAttrMessage, "", "", rowStatus);
                        attributes.addAttribute("", XmlAttrType, "", "", failureType);
                        break;

                    case warning:
                        xmlElement = XmlEleWarning;
                        rowMessage = rowStatus;
                        break;

                    case info:
                        xmlElement = XmlEleInfo;

                        break;

                    default:
                        xmlElement = XmlEleInfo;
                        rowMessage = rowStatus;
                }

                if (attributes.getLength() > 0 || !StringUtil.nullOrEmptyOrBlankString(rowMessage)) {
                    m_writer.startElement("", xmlElement, "", attributes);
                    m_writer.characters(rowMessage);
                    m_writer.endElement(xmlElement);
                }

                /*   end-test case   */
                m_writer.endElement(XmlEleTestCase);

            }

            /*   end-test suite   */
            m_writer.endElement(XmlEleTestSuite);
            m_writer.endDocument();
        } catch (SAXException e) {
            Log.test.error("failure writing to junit output file  %s %e", e, e);
        }
    }

}

/**
 * description: cache junit testcase responses in a listFiles of responses, for aggregation and processing at test suite
 * end.
 */
class XMLJUnitResponseRow {
    private String[] m_values;
    private XMLJUnitResponseMsg m_message;


    public XMLJUnitResponseRow(Object args[], XMLJUnitResponseMsg message) {
        setAttributes(args);
        m_message = message;
    }


    public String getAttribute(int index) {
        if (index >= 0 && index < m_values.length) {
            return m_values[index];
        }

        return "";
    }


    public void setAttributes(Object args[]) {
        m_values = StringUtil.objectArrayToString(args, "");
    }


    public XMLJUnitResponseMsg getMessage() {
        return m_message;
    }
}