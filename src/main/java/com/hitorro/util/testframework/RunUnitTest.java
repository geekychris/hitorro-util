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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Run a junit test from the command line
 *
 * @author chris
 * <p/>
 * rununittest class=ht.test.test.HelloWorldTest;
 */
@CommandDef(command = "test.run", description = "Run unit tests")
public class RunUnitTest extends com.hitorro.util.commandandcontrol.Command {
    @CommandArgument(required = true)
    private StringProperty s_class = new StringProperty("class", "junit test", null);
    @CommandArgument(required = false)
    private BooleanProperty s_all = new BooleanProperty("all", "print all fields", false);

    @Override
    public boolean execute(String rawValue, JVS args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        String clazz = s_class.apply(args);
        Object t = ClassUtil.getInstanceSwallowError(clazz);
        TestResult result = null;

        Class execMe = null;
        String className = s_class.apply(args);
        try {
            execMe = Class.forName(className);
        } catch (ClassNotFoundException e) {
            this.writeSimpleError(response, "Unable to find class %s", className);
        }
        if (t instanceof TestCase) {
            TestCase test = ((TestCase) t);
            // must pass class to constructor for it to put all the test classes.
            TestSuite suite = new TestSuite(execMe);
            suite.setName("Test Suite");
            com.hitorro.util.commandandcontrol.JUnitListener l = new com.hitorro.util.commandandcontrol.JUnitListener(response, s_all.apply(args));
            result = new TestResult();
            result.addListener(l);
            suite.run(result);
            response.end();
        } else {
            writeSimpleError(response, "Class %s is not a test", clazz);
            return false;
        }

        return true;
    }
}

