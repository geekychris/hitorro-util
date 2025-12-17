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
package com.hitorro.util.commandandcontrol.ano;

import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.responsemappings.SingleObjectToStringMapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the Command with basic information such as its description and verb.
 * Can be used with the extensive sub classing of Command class or as annotation on a single method.  Where the method
 * does not know anything about the command framework nor property keys.
 * <p>
 * In the case of a method level invocation to register the method you have the following options:
 * <p>
 * 1) Specifically call CommandRegistry.getRegistry().addAllFromClass(Class);
 * This will register all static methods on a class.  You shouldnt call this if you are trying to access member functions.
 * <p>
 * 2)                   CommandRegistry.getRegistry().addAllFromObject(Object);
 * This will register all static and instance methods.  The command framework will hold onto the instance you have specified.
 * <p>
 * 3) Service level definition with the Class[] debugCommandClasses argument.  This allows you to provide an array of classes
 * that will be registered with the registration of that service.  This has the shortcoming of 1).
 * <p>
 * 4) Service itself can have methods either static of instance based that automatically get registered on init.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDef {
    /**
     * The name of the command such as "env.time"
     *
     * @return
     */
    String command();

    /**
     * Detailed description of the command.
     *
     * @return
     */
    String description();

    /**
     * should this command be treated as invisible to the outside world?
     *
     * @return
     */
    boolean isInternal() default true;

    /**
     * Determines what kind of rest calls can be used with this.  It is possible that a command can support such things
     * as Post or put (requires extra coding on the part of the programmer)
     *
     * @return
     */
    RestOperations[] restOperations() default {RestOperations.Get};

    /**
     * If the command is a definition for
     *
     * @return
     */
    Class resultMapper() default SingleObjectToStringMapping.class;
}
