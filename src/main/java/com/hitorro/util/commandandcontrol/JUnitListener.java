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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.testframework.TestCaseWrapper;
import com.hitorro.util.testframework.TestUtil;
import junit.framework.*;
import org.junit.runner.Description;

/**
 */
public class JUnitListener implements TestListener {
    private Response m_response;
    private String m_class;
    private String m_name;
    private String m_email;
    private String runLevel;
    private String m_description;
    private String m_error;
    private Timer m_timer = new Timer(this.getClass().toString());
    private Float m_time = new Float(0.000);

    private int errorCount = 0;

    private boolean allFields;


    public JUnitListener(Response resp, boolean dumpAllFields) {
        m_response = resp;
        allFields = dumpAllFields;
        addHeader();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public ResponseShape getAllFields() {
        ResponseShape header = new ResponseShape("tests", "tests");
        header.addHeader("TestName", "Author", "Class", "State", "Description", "Time (s)");
        header.addHeaderShortNames("name", "author", "class", "state", "description", "secs");
        header.addRowTypes(String.class, String.class, String.class, String.class, String.class, Float.class);
        return header;
    }

    public ResponseShape getFields() {
        ResponseShape header = new ResponseShape("tests", "test");
        header.addHeader("Method", "Author", "State");
        header.addHeaderShortNames("method", "author", "state");
        header.addRowTypes(String.class, String.class);
        return header;
    }

    public void addHeader() {
        if (allFields) {
            m_response.setResponseShape(getAllFields());
        } else {
            m_response.setResponseShape(getFields());
        }
    }


    public void addError(Test t, Throwable arg1) {
        m_timer.stop();
        Log.test.info("Test %s had an error %s", t, arg1);
        m_error = arg1.getMessage();
        if (StringUtil.nullOrEmptyString(m_error)) {
            m_error = "failure";
        }
        m_response.addInfo(InfoLevel.Error, Fmt.S("Error running test %s with exception %s %e", t, arg1, arg1));
        errorCount++;
    }


    public void addFailure(Test arg0, AssertionFailedError arg1) {
        m_timer.stop();
        Log.test.info("Test %s had a failure %s", arg0, arg1);
        m_error = arg1.getMessage();
        if (StringUtil.nullOrEmptyString(m_error)) {
            m_error = "failure";
        }
        m_response.addInfo(InfoLevel.Error, Fmt.S("Failure running test %s with exception %s %e", arg0, arg1, arg1));
        errorCount++;
    }


    public void endTest(Test arg0) {
        m_timer.stop();
        m_time = m_timer.getTime() / 1000.0F;

        Log.test.info("Test %s finished", arg0);

        if (allFields) {
            m_response.addRow(m_name, m_email, m_class, m_error, m_description, m_time);
        } else {
            m_response.addRow(m_name, m_email, m_error);
        }
    }


    public void startTest(Test arg0) {
        TestUtil.timer.reset(arg0.getClass().getCanonicalName(), arg0);
        m_timer.reset();
        m_timer.start();
        aquireBasicInfo(arg0);
    }


    private void aquireBasicInfo(Test t) {
        m_class = "N/A";
        m_email = "N/A";
        runLevel = "N/A";
        m_description = "N/A";
        m_error = "success";
        if (t instanceof TestCase) {
            TestCase tc = (TestCase) t;
            Class clazz = t.getClass();
            fillFromTestClass(clazz, tc.getName());
        } else if (t instanceof JUnit4TestCaseFacade) {
            Description desc = ((JUnit4TestCaseFacade) t).getDescription();
            Class c = desc.getTestClass();
            fillFromTestClass(c, desc.getMethodName());
        } else {
            Log.test.fatal("We are not handling a new type of test being passed to this listener %s", t.getClass());
        }
    }

    private void fillFromTestClass(final Class clazz, String name) {
        m_name = Fmt.S("%s.%s", clazz.getSimpleName(), name);
        m_class = clazz.toString();

        TestCaseWrapper wrapper = new TestCaseWrapper(clazz);
        if (wrapper.testDef != null) {
            m_email = wrapper.testDef.email();
            runLevel = wrapper.testDef.runlevel().toString();
            m_description = wrapper.testDef.description();
        }
    }

}
