package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.Env;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "env.processstats", description = "Dump various stats about the running process")
public class DumpProcessStats extends Command {
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> map = new TreeMap<String, Object>();
        Env.getBasicProcessInfo(map, threadMXBean);
        Env.getOSInfo(map);
        Env.getHeapMemoryInfo(map);
        Env.getGCInfo(map);
        Env.getJavaVMInfo(map);
        this.writeMap(response, getKVShape(), map);
        return true;
    }


}
