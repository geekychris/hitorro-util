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
package ht.util.startupframework.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a whole bunch of things about a service, such as what classes and interfaces that the service depends on,
 * what commands it needs to initialize, what persisted types it defines, its description and a name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface ServiceDefinition {
    /**
     * classes that this service definition is dependent on.  Must implement a Service or a ServiceDefinition
     *
     * @return
     */
    Class[] dependentService() default {};

    /**
     * interfaces that this service is dependent on, that is rather then depending on a specific service, it could be
     * dependent on a class that implements an interface.  Dependencies here can not be dragged in automatically by this
     * definition.
     *
     * @return
     */
    Class[] dependentServiceInterfaces() default {};

    /**
     * short name of the service.
     *
     * @return
     */
    String shortName();

    /**
     * description of the service
     *
     * @return
     */
    String description();

    /**
     * If this class interacts with a persistence subsystem, such as hibernate or something that needs to identify
     * classes for marshalling, then define the classes here
     *
     * @return
     */
    Class[] typeManagedClasses() default {};

    /**
     * debug commands that need to be initialized.  Initialization of a command must be via an anonymous constructor.
     * Further that command must overide the setFromService method if it needs to know about the service that initially
     * referenced it.
     *
     * @return
     */
    Class[] debugCommands();

    /**
     * List of classes that have static methods that should be added to the command set.
     *
     * @return
     */
    Class[] debugCommandClasses() default {};

    /**
     * List of directories that are part of the UI layer, only to be copied in if they are part of the running
     * application.
     *
     * @return
     */
    String[] uiDirectories() default {};

    /**
     * If the listFiles of services must be automatically generated, call the getDependentServices method instead.
     *
     * @return
     */
    boolean generatedServices() default false;

    /**
     * If the scheduler service is active, providing a path within the configs here causes the scheduler  to put jobs from
     * config path.
     *
     * @return
     */
    String[] scheduledJobPath() default {};

    /**
     * scripts that should be run on dbinit.  These are scripts that finalizeFilter the setup of a server if the dbinit flag
     * was true during startup.  NOTE that currently these execute post start
     *
     * @return
     */
    String[] dbInitShellScripts() default {};
}
