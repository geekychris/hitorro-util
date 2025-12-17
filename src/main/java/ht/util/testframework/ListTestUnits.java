package ht.util.testframework;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.ArrayUtil;
import ht.util.core.opers.LogicalAndOperator;
import ht.util.json.keys.BaseMappingProperty;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 29, 2006 Time: 9:09:22 AM
 */
@CommandDef(command = "test.listtests", description = "List the available unit tests")
public class ListTestUnits extends Command {
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
    private ResponseShape header = new ResponseShape();

    public ListTestUnits() {

    }

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        List<TestCaseWrapper> cases = null;

        LogicalAndOperator<TestCaseWrapper> oper = TestUtil.getOperator(args.getJsonNode());
        try {
            cases = FilteredTestSuiteGenerator.getTestCases(oper, null, TestUtil.IgnoreTestDependencies.apply(args));

        } catch (TestException e) {
            this.writeSimpleError(response, e.getMessage());
            return false;
        }

        response.setResponseShape(header);
        MultiRowResponse mrm = response.getMultiRowResponse();
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