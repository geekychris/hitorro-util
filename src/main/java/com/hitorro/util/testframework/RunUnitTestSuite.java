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
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.json.keys.BaseMappingProperty;
import com.hitorro.util.json.keys.BooleanProperty;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 4:46:07 PM
 */
@CommandDef(command = "test.runsuite", description = "Run a suite of tests")

public class RunUnitTestSuite extends com.hitorro.util.commandandcontrol.Command {
    @CommandArgument(required = true)
    public static final BaseMappingProperty level = TestUtil.Level;
    @CommandArgument(required = false)
    public static final BaseMappingProperty pack = TestUtil.Packages;
    @CommandArgument(required = false)
    public static final BaseMappingProperty dep = TestUtil.IgnoreTestDependencies;
    @CommandArgument(required = false)
    public static final BaseMappingProperty service = TestUtil.Services;
    @CommandArgument(required = false)
    public static final BaseMappingProperty module = TestUtil.Modules;
    @CommandArgument(required = false)
    public static final BaseMappingProperty tags = TestUtil.Modules;
    @CommandArgument(required = false)
    public static final BaseMappingProperty tests = TestUtil.Test;
    private BooleanProperty s_all = new BooleanProperty("all", "print all fields", false);

    @Override
    public boolean execute(String rawValue, JVS args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        TestSuite suite = null;
        try {
            LogicalAndOperator<TestCaseWrapper> oper = TestUtil.getOperator(args.getJsonNode());
            suite = FilteredTestSuiteGenerator.getTestSuite(oper, null, TestUtil.IgnoreTestDependencies.apply(args));
        } catch (TestException e) {
            this.writeSimpleError(response, e.getMessage());
            return false;
        }
        TestResult result = null;
        suite.setName("Test Suite");
        com.hitorro.util.commandandcontrol.JUnitListener l = new com.hitorro.util.commandandcontrol.JUnitListener(response, s_all.apply(args));
        result = new TestResult();
        result.addListener(l);
        suite.run(result);
        response.end();

        return true;
    }
}