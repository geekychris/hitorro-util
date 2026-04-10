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
package com.hitorro.util.servicecounters;

import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.servicecounters.registers.DoubleDivideRegister;
import com.hitorro.util.servicecounters.registers.DoubleRegister;
import com.hitorro.util.servicecounters.registers.LongImplementableRegister;
import com.hitorro.util.servicecounters.registers.LongRegister;
import com.hitorro.util.startupframework.phases.ServiceDefinition;


@ServiceDefinition(dependentService = {},
        shortName = "counters",
        description = "Counters service",
        debugCommands = {PrintCounters.class},
        typeManagedClasses = {},
        uiDirectories = {},
        dependentServiceInterfaces = {})
public class CounterService {
    public static LongRegister tick;
    public static DoubleRegister tickD;
    public static DoubleDivideRegister tickDiv;
    public static LongImplementableRegister currentTime;
    private static CounterService service;
    private RestartableService m_rs;
    private CounterClock counterClock = new CounterClock(CounterContext.getContext());
    private CounterSet testSet = new CounterSet("test");

    public static final CounterService getService() {
        return service;
    }

    public CounterContext getCounterContext() {
        return CounterContext.getContext();
    }

    public CounterClock getClock() {
        return counterClock;
    }

    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        service = this;
        m_rs = new RestartableService("CounterService", "Counters", 100, counterClock, true);
        RestartableServiceDaemon.addService(m_rs);
        tick = testSet.getLongRegister("tick", "Test Tick");

        tickD = testSet.getDoubleRegister("tickd", "Test Tick as double");
        tickDiv = testSet.getDoubleDivideRegister("tickdivision", "Test Tick as double", tick, tickD);
        /*currentTime = new LongImplementableRegister(testSet, "currentTime", "demonstrates implementable register")
        {
            public long getAsLong ()
            {
                return System.currentTimeMillis();
            }

            public String apply ()
            {
                return foo();
            }

            public String foo ()
            {
                return UnitTimeContext.getUnitTimeContext().executeTest(new DoubleArraySum()).toString();
            }
        };*/
        testSet.finishInit(CounterContext.getContext());

        return null;
    }

    public String start(boolean dbInit) {
        return null;
    }

    public String deInit() {
        return null;
    }
}
