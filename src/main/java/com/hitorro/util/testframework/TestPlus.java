package com.hitorro.util.testframework;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface TestPlus {
	public static final String TestDataOutput = "testoutput";

	public static final String TestData = "testinput";


	/**
			* Absolute path to the TESTDATA property
     *
			 * @return
			 */
	default File getInputTreeRoot() {
		File bin = Env.getBin();
		if (bin != null) {
			return new File(bin, TestData);
		}
		return null;
	}

	default BaseFile getInputTreeRootBaseFile() {
		return FileFileSystem.Root.getFile(getOutputTreeRoot().getAbsolutePath());
	}

	static File getOutputTreeRoot() {
		File home = Env.getHome();
		if (home != null) {
			return new File(home, TestDataOutput);
		}
		return null;
	}

	default BaseFile getOutputTreeRootBaseFile() {
		return FileFileSystem.Root.getFile(getInputTreeRoot().getAbsolutePath());
	}

	default File getInputFileRelativeForClass(String fileName, Class clazz, boolean includeClassName) {
		return new File(getInputTreeRoot(), Fmt.S("%s%s%s",
				getClassPathAsPath(clazz, includeClassName), File.separator, fileName));
	}

	static File getOutputFileRelativeForClass(String fileName, Class clazz, boolean includeClassName) {
		return new File(getOutputTreeRoot(), Fmt.S("%s%s%s", getClassPathAsPath(clazz, includeClassName), File.separator, fileName));
	}

	static String getClassPathAsPath(Class clazz, boolean includeClassName) {
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
	default void prolongTestTimeout(int seconds) {
		Log.test.info("Test:%s is prolonging the watchdog timeout by: %s", this.getClass(), seconds);
		TestUtil.timer.prolongTest(seconds);
	}

	default File getInputFileRelative(String fileName, boolean includeClassName) {

		return new File(getInputTreeRoot(), Fmt.S("%s%s%s",
				getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
	}

	default BaseFile getInputFileRelativeBaseFile(String fileName, boolean includeClassName) {

		return FileFileSystem.Root.getFile(Fmt.S("%s/%s%s%s", getInputTreeRoot().getAbsolutePath(),
				getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
	}

	default File getInputFileRelative(String fileName) {
		return getInputFileRelative(fileName, false);
	}

	default BaseFile getInputFileRelativeBaseFile(String fileName) {
		return getInputFileRelativeBaseFile(fileName, false);
	}

	default File getOutputFileRelative(String fileName, boolean includeClassName) {
		File dir = new File(getOutputTreeRoot(), getClassPathAsPath(this.getClass(), includeClassName));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(getOutputTreeRoot(), Fmt.S("%s%s%s",
				getClassPathAsPath(this.getClass(), includeClassName), File.separator, fileName));
	}

	default BaseFile getOutputFileRelativeBaseFile(String fileName, boolean includeClassName) {
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
	default File getOutputDirViaInputDirCopy(String name) throws IOException {
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
	default BaseFile getOutputDirViaInputDirCopyBaseFile(String name) throws IOException {
		File in = this.getInputFileRelative(name);
		File out = this.getOutputFileRelative(name);
		this.assertFileExistsWithExplanation(in);
		FileUtil.deleteDirectoryContent(out, true);
		FileUtil.ensureDirectoryExists(out);
		FileUtil.copyDirectory(in, out);
		return FileFileSystem.Root.getFile(out.getAbsolutePath());
	}

	default File getOutputFileRelative(String fileName) {
		return getOutputFileRelative(fileName, false);
	}

	default BaseFile getOutputFileRelativeBaseFile(String fileName) {
		return getOutputFileRelativeBaseFile(fileName, false);
	}

	default void assertMapContains(Map m, Object v) {
		if (m.get(v) == null) {
			assertAlways("expected apply to contain %s", v);
		}
	}

	default void assertStartsWith(String explanation, String test, String startsWithThis) {
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
	default void assertLinesOfFilesEqual(String explanation, File a, File b, String encoding) {
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
	default void assertBinaryFilesEqual(String explanation, File a, File b) {
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
	default void assertBinaryFilesEqual(String explanation, BaseFile a, BaseFile b) {
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
	default void assertFileExistsWithExplanation(File a) {
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
	default void assertFileExistsWithExplanation(BaseFile a) {
		if (a == null) {
			TestCase.assertEquals("null provided to file exists", 1, 0);
		}
		if (!BaseFile.notNullAndExists(a)) {
			TestCase.assertEquals(Fmt.S("File %s does not exist", a), 1, 0);
		}
	}

	default void assertFileDoesNotExistWithExplanation(File a) {
		if (a == null) {
			TestCase.assertEquals("null provided to file exists", 1, 0);
		}
		if (a.exists()) {
			TestCase.assertEquals(Fmt.S("File %s exists", a), 1, 0);
		}
	}

	default void assertSameType(Object o, Class c) {
		TestCase.assertTrue(ClassUtil.isSubClass(o.getClass(), c));
	}

	default void assertEqualsIgnoreCase(String s1, String s2) {
		TestCase.assertTrue(StringUtil.notNullEquals(s1, s2, true));
	}


	/**
	 * We have determined we have failed a test.
	 *
	 * @param message
	 * @param args
	 */
	default void assertAlways(String message, Object... args) {
		TestCase.assertTrue(Fmt.S(message, args), false);
	}


	default void addPropsFromFile(File file) {
		HTProperties.getProperties().readFile(file, false);
	}


	/**
	 * Reload the system properties
	 */
	default void reloadConfig() {
		BaseCommandLine.getCommandLine().reloadJVSProps(true);
	}
}
