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
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.json.keys.BaseMappingProperty;

import java.util.List;

/**
 */
@CommandDef(command = "test.listtests", description = "List the available unit tests")
public class ListTestUnits extends com.hitorro.util.commandandcontrol.Command {
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
    public static final BaseMappingProperty tests = TestUtil.Modules;
    @ResponseDefinition(command = "tests",
            rowname = "test",
            columns = {@RespColumn(name = "Class", lName = "class"),
                    @RespColumn(name = "Level", lName = "level"),
                    @RespColumn(name = "Description", lName = "desc"),
                    @RespColumn(name = "Service depends", lName = "servicedeps"),
                    @RespColumn(name = "Test depends", lName = "testdeps")})
    private com.hitorro.util.commandandcontrol.ResponseShape header = new com.hitorro.util.commandandcontrol.ResponseShape();

    public ListTestUnits() {

    }

    @Override
    public boolean execute(String rawValue, JVS args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        List<TestCaseWrapper> cases = null;

        LogicalAndOperator<TestCaseWrapper> oper = TestUtil.getOperator(args.getJsonNode());
        try {
            cases = FilteredTestSuiteGenerator.getTestCases(oper, null, TestUtil.IgnoreTestDependencies.apply(args));

        } catch (TestException e) {
            this.writeSimpleError(response, e.getMessage());
            return false;
        }

        response.setResponseShape(header);
        com.hitorro.util.commandandcontrol.MultiRowResponse mrm = response.getMultiRowResponse();
        for (TestCaseWrapper c : cases) {

            mrm.add(0, c.getClass().getCanonicalName());
            mrm.add(1, c.testDef.runlevel().toString());
            mrm.add(2, c.testDef.description());
            Class d[] = c.testDef.dependentServices();
            if (!ArrayUtil.nullOrEmpty(d)) {
                for (Class cl : d) {
                    mrm.add(3, cl.getName());
                }
            }
            Class t[] = c.testDef.dependentTests();
            if (!ArrayUtil.nullOrEmpty(t)) {
                for (Class cl : t) {
                    mrm.add(4, cl.getName());
                }
            }
            mrm.addToResponse(response);
            mrm.clear();
        }
        response.end();

        return true;
    }
}