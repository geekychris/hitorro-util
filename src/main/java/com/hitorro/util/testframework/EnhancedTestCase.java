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
package com.hitorro.util.testframework;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Platform;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Extension to the JUnit test case that offers a few extras: - part of the unit test auto discovery framework, which
 * will run tests at a specific run level. - provides a Input directory tree (imutable) and output directory (mutable)
 * to allow consistent data file reading and possible canonical file diffing (rather than the simple assert model). -
 * numerous assert methods for convenience.
 * <p/>
 * <p/>
 * Some other test mechanisms:
 * <p/>
 * - what about asserts based upon instrumentation: http://modernjass.sourceforge.net/
 * <p/>
 * subclassing an enhancedtestcase means that you should use the old junit3 convention for test definitions that being
 * the bean style method naming.  You can alternatively tag a class with #HTTestRunnable
 */
public abstract class EnhancedTestCase extends TestCase implements HTTestRunnable, TestPlus {
    public static final String TestDataOutput = "testoutput";

    public static final String TestData = "testinput";

    public static Platform[] platforms;


    public EnhancedTestCase() {
        super();
    }

    public static final void assertNotNull(Object o) {
        TestCase.assertNotNull(o);
    }

    public static final void assertNotNull(String msg, Object o) {
        TestCase.assertNotNull(msg, o);
    }

    public static final void assertEquals(String msg, String s, String s2) {
        TestCase.assertEquals(msg, s, s2);
    }

    public static final void assertEquals(String s, String s2) {
        TestCase.assertEquals(s, s2);
    }

    public static final void assertEquals(Class s, Class s2) {
        TestCase.assertEquals(s, s2);
    }

    public static final void assertEquals(Object s, Object s2) {
        TestCase.assertEquals(s, s2);
    }

    public static final void assertEquals(String msg, Object s, Object s2) {
        TestCase.assertEquals(msg, s, s2);
    }

    public static final void assertTrue(boolean b) {
        TestCase.assertTrue(b);
    }

    public static final void assertTrue(String msg, boolean b) {
        TestCase.assertTrue(msg, b);
    }

    public static final void assertEquals(int a, int b) {
        TestCase.assertEquals(a, b);
    }

    public static final void assertEquals(String msg, int a, int b) {
        TestCase.assertEquals(msg, a, b);
    }

    public static final void assertEquals(long a, long b) {
        TestCase.assertEquals(a, b);
    }

    public static final void assertEquals(String msg, long a, long b) {
        TestCase.assertEquals(msg, a, b);
    }

    public static final void assertFalse(boolean b) {
        TestCase.assertFalse(b);
    }

    public static final void assertFalse(String msg, boolean b) {
        TestCase.assertFalse(msg, b);
    }

    public static final void assertNull(Object o) {
        TestCase.assertNull(o);
    }

    public static final void assertNull(String msg, Object o) {
        TestCase.assertNull(msg, o);
    }

    public static final void fail(String msg) {
        TestCase.fail(msg);
    }
}
