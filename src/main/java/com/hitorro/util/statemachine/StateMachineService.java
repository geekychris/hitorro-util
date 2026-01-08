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
package com.hitorro.util.statemachine;

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.io.filefilters.FileStartsEndsWith;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@ServiceDefinition(dependentService = {},
        shortName = "statemachine",
        description = "State machine service",
        debugCommands = {},
        typeManagedClasses = {State.class, Group.class, DirectedEdge.class},
        uiDirectories = {},
        dependentServiceInterfaces = {})
public class StateMachineService {
    public static final FileProperty StateMachineDir = new FileProperty("statemachine.dir", "State machines", "${HT_BIN}/data/statemachine/");
    private static StateMachineService s_service;
    private Map<String, MooreStateMachine> stateMachines = new HashMap<String, MooreStateMachine>();

    public static StateMachineService getService() {
        return s_service;
    }

    public synchronized MooreStateMachine getStateMachine(String name) {
        name = name.toLowerCase();
        MooreStateMachine sc = stateMachines.get(name);
        if (sc != null) {
            return sc;
        }
        File dir = StateMachineDir.apply();
        FileStartsEndsWith filter = new FileStartsEndsWith(name, true, false);
        File files[] = dir.listFiles(filter);
        if (ArrayUtil.nullOrEmpty(files)) {
            return null;
        }
        sc = StateMachineUtil.initStateRegistry(files[0]);
        if (sc != null) {
            stateMachines.put(name, sc);
        }
        return sc;
    }

    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        s_service = this;
        return null;
    }

    public String start(boolean dbInit) {
        return null;
    }

    public String deInit() {
        return null;
    }
}
