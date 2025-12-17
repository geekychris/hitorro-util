package ht.util.testframework;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.classes.ClassUtil;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.StringProperty;
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
public class RunUnitTest extends Command {
    @CommandArgument(required = true)
    private StringProperty s_class = new StringProperty("class", "junit test", null);
    @CommandArgument(required = false)
    private BooleanProperty s_all = new BooleanProperty("all", "print all fields", false);

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
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
            JUnitListener l = new JUnitListener(response, s_all.apply(args));
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

