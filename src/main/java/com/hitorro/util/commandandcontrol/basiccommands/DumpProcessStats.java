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
package com.hitorro.util.commandandcontrol.basiccommands;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.Env;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.TreeMap;


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
