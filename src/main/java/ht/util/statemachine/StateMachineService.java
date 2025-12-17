package ht.util.statemachine;

import ht.util.core.ArrayUtil;
import ht.util.io.filefilters.FileStartsEndsWith;
import ht.util.json.keys.FileProperty;
import ht.util.startupframework.phases.ServiceDefinition;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
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
