package ht.util.testframework;

import ht.util.core.Platform;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HTTest {
    Platform[] platform() default {Platform.Linux, Platform.MacOSX, Platform.Windows, Platform.Solaris, Platform.Other};

    /**
     * @return testcase run levels
     */
    RunLevel runlevel() default RunLevel.Smoke;

    /**
     * Contact information for who wrote the test.  if test failure, then author is to be automatically emailed.
     *
     * @return
     */
    String email();

    String module() default "";

    /**
     * Description of the purpose of the test case.
     *
     * @return
     */
    String description();

    /**
     * List of tests that must be run before this.  Note that they will only be used to order the tests, if they are out
     * of scope (such as a higher level test, we will only warn, this is because you may run an individual test and of
     * run a precursor test some time before, it would be mighty annoying if we forced you to load the database with
     * data when you know its already there. In a full test run though, these types of warnings are probably a very bad
     * sign.
     *
     * @return
     */
    Class[] dependentTests() default {};

    /**
     * List of services that must be available for this test to run
     *
     * @return
     */
    Class[] dependentServices() default {};

    /**
     * @return arbitrary tags with which to filter testcases
     */

    String[] tags() default {};
}
