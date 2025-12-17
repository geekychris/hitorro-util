package ht.util.testframework;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.opers.LogicalAndOperator;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.json.keys.BooleanProperty;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 4:46:07 PM
 */
@CommandDef(command = "test.runsuite", description = "Run a suite of tests")

public class RunUnitTestSuite extends Command {
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
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
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
        JUnitListener l = new JUnitListener(response, s_all.apply(args));
        result = new TestResult();
        result.addListener(l);
        suite.run(result);
        response.end();

        return true;
    }
}