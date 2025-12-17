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

import com.hitorro.util.core.Platform;

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
