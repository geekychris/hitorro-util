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
public abstract class EnhancedTestCase extends TestCase implements HTTestRunnable {
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

    /**
     * Absolute path to the TESTDATA property
     *
     * @return
     */
    public static File getInputTreeRoot() {
        File bin = Env.getBin();
        if (bin != null) {
            return new File(bin, TestData);
        }
        return null;
    }

    public static BaseFile getInputTreeRootBaseFile() {
        return FileFileSystem.Root.getFile(getOutputTreeRoot().getAbsolutePath());
    }

    public static File getOutputTreeRoot() {
        File home = Env.getHome();
        if (home != null) {
            return new File(home, TestDataOutput);
        }
        return null;
    }

    public static BaseFile getOutputTreeRootBaseFile() {
        return FileFileSystem.Root.getFile(getInputTreeRoot().getAbsolutePath());
    }

    public static File getInputFileRelativeForClass(String fileName, Class clazz, boolean includeClassName) {
        return new File(getInputTreeRoot(), Fmt.S("%s%s%s",
                getClassPathAsPath(clazz, includeClassName), File.separator, fileName));
    }

    public static File getOutputFileRelativeForClass(String fileName, Class clazz, boolean includeClassName) {
        return new File(getOutputTreeRoot(), Fmt.S("%s%s%s", getClassPathAsPath(clazz, includeClassName), File.separator, fileName));
    }

    public static String getClassPathAsPath(Class clazz, boolean includeClassName) {
        String name;


        if (clazz == null) {

            clazz = EnhancedTestCase.class;
        }

        if (includeClassName) {
            name = clazz.getName();
        } else {
            Package packageName = clazz.getPackage();
            name = packageName.getName();
        }

        return name.replace(".", File.separator);
    }

    /**
     * If a test knows that it takes a very long time, it can periodically reset the timeout, maybe its a stress test
     *
     * @param seconds
     */
    public void prolongTestTimeout(int seconds) {
        Log.test.info("Test:%s is prolonging the watchdog timeout by: %s", this.getClass(), seconds);
        TestUtil.timer.prolongTest(seconds);
    }

    public File getInputFileRelative(String fileName, boolean includeClassName) {

        return new File(getInputTreeRoot(), Fmt.S("%s%s%s",
                getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
    }

    public BaseFile getInputFileRelativeBaseFile(String fileName, boolean includeClassName) {

        return FileFileSystem.Root.getFile(Fmt.S("%s/%s%s%s", getInputTreeRoot().getAbsolutePath(),
                getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
    }

    public File getInputFileRelative(String fileName) {
        return getInputFileRelative(fileName, false);
    }

    public BaseFile getInputFileRelativeBaseFile(String fileName) {
        return getInputFileRelativeBaseFile(fileName, false);
    }

    public File getOutputFileRelative(String fileName, boolean includeClassName) {
        File dir = new File(getOutputTreeRoot(), getClassPathAsPath(this.getClass(), includeClassName));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(getOutputTreeRoot(), Fmt.S("%s%s%s",
                getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
    }

    public BaseFile getOutputFileRelativeBaseFile(String fileName, boolean includeClassName) {
        File dir = new File(getOutputTreeRoot(), getClassPathAsPath(this.getClass(), includeClassName));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return FileFileSystem.Root.getFile(Fmt.S("%s/%s%s%s", getOutputTreeRoot().getAbsolutePath(),

                getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
    }

    /**
     * Convenience mechanism where the input directory needs to be copied so it can be "worked on"
     * by the test.
     *
     * @param name
     * @return
     * @throws IOException
     */
    public File getOutputDirViaInputDirCopy(String name) throws IOException {
        File in = this.getInputFileRelative(name);
        File out = this.getOutputFileRelative(name);
        this.assertFileExistsWithExplanation(in);
        FileUtil.deleteDirectoryContent(out, true);
        FileUtil.ensureDirectoryExists(out);
        FileUtil.copyDirectory(in, out);
        return out;
    }

    /**
     * Convenience mechanism where the input directory needs to be copied so it can be "worked on".
     * <p>
     * This derivative provides a BaseFile
     * <p>
     * by the test.
     *
     * @param name
     * @return
     * @throws IOException
     */
    public BaseFile getOutputDirViaInputDirCopyBaseFile(String name) throws IOException {
        File in = this.getInputFileRelative(name);
        File out = this.getOutputFileRelative(name);
        this.assertFileExistsWithExplanation(in);
        FileUtil.deleteDirectoryContent(out, true);
        FileUtil.ensureDirectoryExists(out);
        FileUtil.copyDirectory(in, out);
        return FileFileSystem.Root.getFile(out.getAbsolutePath());
    }

    public File getOutputFileRelative(String fileName) {
        return getOutputFileRelative(fileName, false);
    }

    public BaseFile getOutputFileRelativeBaseFile(String fileName) {
        return getOutputFileRelativeBaseFile(fileName, false);
    }

    public void assertMapContains(Map m, Object v) {
        if (m.get(v) == null) {
            assertAlways("expected apply to contain %s", v);
        }
    }

    public void assertStartsWith(String explanation, String test, String startsWithThis) {
        if (!test.startsWith(startsWithThis)) {
            assertAlways(explanation);
        }
    }

    /**
     * Almost as bad as the binary differ from expecting absolute likeness, however this should deal well with the canon
     * file containing different cr lf combo's than that generated in the current test run.
     *
     * @param explanation
     * @param a
     * @param b
     * @param encoding
     */
    public void assertLinesOfFilesEqual(String explanation, File a, File b, String encoding) {
        int returnVal;
        try {
            returnVal = FileUtil.lineFileDiffer(a, b, encoding);
        } catch (IOException e) {
            TestCase.assertEquals(explanation, 0, 1);
            return;
        }
        TestCase.assertEquals(explanation, 0, returnVal);
    }


    /**
     * Assert that both files are identical using a binary diffNodeGroupAgainstInstanceList. We examine file size and
     * then bytes to ensure they are the same.
     *
     * @param explanation
     * @param a
     * @param b
     */
    public void assertBinaryFilesEqual(String explanation, File a, File b) {
        int returnVal;
        try {
            returnVal = FileUtil.binaryFileDiff(a, b);
        } catch (IOException e) {
            TestCase.assertEquals(explanation, 0, 1);
            return;
        }
        TestCase.assertEquals(explanation, 0, returnVal);
    }

    /**
     * Assert that both files are identical using a binary diffNodeGroupAgainstInstanceList. We examine file size and
     * then bytes to ensure they are the same.
     *
     * @param explanation
     * @param a
     * @param b
     */
    public void assertBinaryFilesEqual(String explanation, BaseFile a, BaseFile b) {
        int returnVal;
        try {
            returnVal = BaseFileUtil.binaryFileDiff(a, b);
        } catch (IOException e) {
            TestCase.assertEquals(explanation, 0, 1);
            return;
        }
        TestCase.assertEquals(explanation, 0, returnVal);
    }


    /**
     * Assert that the file exists and assert showing the filename itself in the explanation.
     *
     * @param a
     */
    public void assertFileExistsWithExplanation(File a) {
        if (a == null) {
            TestCase.assertEquals("null provided to file exists", 1, 0);
        }
        if (FileUtil.nullOrNotExist(a)) {
            TestCase.assertEquals(Fmt.S("File %s does not exist", a), 1, 0);
        }
    }

    /**
     * Assert that the file exists and assert showing the filename itself in the explanation.
     *
     * @param a
     */
    public void assertFileExistsWithExplanation(BaseFile a) {
        if (a == null) {
            TestCase.assertEquals("null provided to file exists", 1, 0);
        }
        if (!BaseFile.notNullAndExists(a)) {
            TestCase.assertEquals(Fmt.S("File %s does not exist", a), 1, 0);
        }
    }

    public void assertFileDoesNotExistWithExplanation(File a) {
        if (a == null) {
            TestCase.assertEquals("null provided to file exists", 1, 0);
        }
        if (a.exists()) {
            TestCase.assertEquals(Fmt.S("File %s exists", a), 1, 0);
        }
    }

    public void assertSameType(Object o, Class c) {
        TestCase.assertTrue(ClassUtil.isSubClass(o.getClass(), c));
    }

    public void assertEqualsIgnoreCase(String s1, String s2) {
        TestCase.assertTrue(StringUtil.notNullEquals(s1, s2, true));
    }


    /**
     * We have determined we have failed a test.
     *
     * @param message
     * @param args
     */
    public void assertAlways(String message, Object... args) {
        TestCase.assertTrue(Fmt.S(message, args), false);
    }


    public void addPropsFromFile(File file) {
        HTProperties.getProperties().readFile(file, false);
    }


    /**
     * Reload the system properties
     */
    public void reloadConfig() {
        BaseCommandLine.getCommandLine().reloadJVSProps(true);
    }
}
