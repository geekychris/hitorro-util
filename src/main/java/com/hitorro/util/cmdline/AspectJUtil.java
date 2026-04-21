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
package com.hitorro.util.cmdline;

//import com.sun.tools.attach.VirtualMachine;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.classes.ClassPathShallowIterator;
import com.hitorro.util.core.opers.StringContainsOperator;
import com.sun.tools.attach.VirtualMachine;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

public class AspectJUtil {

    public static void setupAspectJ() {
        if (!isAspectJAgentLoaded()) {
            System.err.println("WARNING: AspectJ weaving agent not loaded");
        } else {
            Instrumentation i = getInstrumentation();
            Console.println("hello");
        }
    }

    public static boolean isAspectJAgentLoaded() {
        try {
            Agent.getInstrumentation();
        } catch (NoClassDefFoundError e) {
            System.out.println(e);
            return false;
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
            //return dynamicallyLoadAspectJAgent();
        }
        return true;
    }

    public static Instrumentation getInstrumentation() {
        try {
            return Agent.getInstrumentation();
        } catch (NoClassDefFoundError e) {
            System.out.println(e);
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
        }
        return null;
    }

    public static boolean dynamicallyLoadAspectJAgent ()
    {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try
        {
            VirtualMachine vm = VirtualMachine.attach(pid);
            String jarFilePath = System.getProperty("AGENT_PATH");
            if (jarFilePath == null)
            {
                jarFilePath = getJarFilePath();
            }
            vm.loadAgent(jarFilePath);
            vm.detach();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static final String getJarFilePath ()
    {
        ClassPathShallowIterator iter = new ClassPathShallowIterator();
        return iter.filter(new StringContainsOperator("aspectjweaver-1", true)).getFirstItem();
    }
}
