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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.commandandcontrol.ano.*;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.thread.HTThread;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**

 * Dump all the threads running in the system.  Also is capable of dumping stack frames and
 */
@com.hitorro.util.commandandcontrol.ano.CommandDef(command = "env.threads", description = "Dump the threads running in the system and optionally their stack frames.")
public class DumpThreads extends com.hitorro.util.commandandcontrol.Command {
    @com.hitorro.util.commandandcontrol.ano.CommandArgument(required = false)
    public static final BooleanProperty DumpStack = new BooleanProperty("stack", "Dump the thread stack", false);
    @com.hitorro.util.commandandcontrol.ano.CommandArgument(required = false)
    public static final BooleanProperty Desc = new BooleanProperty("description", "thread dump with groups and possible thread info", false);
    @com.hitorro.util.commandandcontrol.ano.CommandArgument(required = false)
    public static final BooleanProperty DumpDeadlock = new BooleanProperty("dumpdeadlock", "Dump deadlocked threads", false);
    @com.hitorro.util.commandandcontrol.ano.CommandArgument(required = false)
    public static final StringProperty Ids = new StringProperty("ids", "List of thread ids to constrain by", null);

    @com.hitorro.util.commandandcontrol.ano.ResponseDefinition(command = "dumpthreads",
            rowname = "thread",
            columns = {@com.hitorro.util.commandandcontrol.ano.RespColumn(name = "frame", lName = "frame"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "ID", lName = "id", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "State", lName = "state"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "CPUTime", lName = "cputime", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "LockName", lName = "lockName"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "LockOwnerId", lName = "lockOwnerId", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "BlockedCount", lName = "blockedCount"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "BlockedTime", lName = "blockedTime", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "WaitedCount", lName = "waitedCount", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "WaitedTime", lName = "WaitedTime", type = Long.class)},
            groups = {@com.hitorro.util.commandandcontrol.ano.ColumnGroup(start = 0, size = 1, name = "stack")})
    protected static com.hitorro.util.commandandcontrol.ResponseShape header = new com.hitorro.util.commandandcontrol.ResponseShape();

    @com.hitorro.util.commandandcontrol.ano.ResponseDefinition(command = "dumpthreads",
            rowname = "thread",
            columns = {@com.hitorro.util.commandandcontrol.ano.RespColumn(name = "Type", lName = "type"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "Parent", lName = "parent"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "Thread", lName = "thread"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "Id", lName = "id", type = Long.class),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "Priority", lName = "priority"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "isDaemon", lName = "isdaemon"),
                    @com.hitorro.util.commandandcontrol.ano.RespColumn(name = "isAlive", lName = "isalive")})
    protected static com.hitorro.util.commandandcontrol.ResponseShape headerInfo = new com.hitorro.util.commandandcontrol.ResponseShape();

    com.hitorro.util.commandandcontrol.RenderingContainer redLine[];

    /**
     * If we need to disable counters because they are a perf issue, we can enable this at some point.
     */
    @SuppressWarnings("unused")
    private static final void disableCounters() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadCpuTimeEnabled(false);
        threadMXBean.setThreadContentionMonitoringEnabled(false);
    }

    public void init() {
        redLine = new com.hitorro.util.commandandcontrol.RenderingContainer[header.getHeaderLong().length];
        for (int i = 0; i < redLine.length; i++) {
            redLine[i] = new com.hitorro.util.commandandcontrol.RenderingContainer(com.hitorro.util.commandandcontrol.ColorEnum.blue, com.hitorro.util.commandandcontrol.ColorEnum.red);
        }
    }

    private long[] getIds(JsonNode args) {
        String idsString = Ids.apply(args);
        long ids[] = null;
        if (!StringUtil.nullOrEmptyOrBlankString(idsString)) {
            String idsArray[] = StringUtil.tokenizeFromSingleChar(idsString, ",", true);
            ids = ArrayUtil.toLong(idsArray);
        }
        return ids;
    }

    private String stackFrameRow(StackTraceElement elements[], int index) {
        if (elements == null || elements.length == 0 || index > elements.length) {
            return "";
        }
        StackTraceElement element = elements[index];
        return Fmt.S("%s.%s(%s) ", element.getClassName(), element
                .getMethodName(), element.getLineNumber());
    }

    @Override
    public boolean execute(String rawValue, JVS args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        if (Desc.apply(args)) {
            listAllThreadsSet(response);
            response.end();
            return true;
        }

        int stackDepth = 0;
        boolean dumpDeadlock = DumpDeadlock.apply(args);
        long ids[] = getIds(args.getJsonNode());
        if (DumpStack.apply(args)) {
            stackDepth = 100;
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        long[] threads;
        if (!dumpDeadlock) {
            threads = threadMXBean.getAllThreadIds();
        } else {
            threads = threadMXBean.findMonitorDeadlockedThreads();
        }

        response.setResponseShape(header);
        if (dumpDeadlock && threads == null) {
            response.addInfo(com.hitorro.util.commandandcontrol.InfoLevel.Info, "No deadlocks found");
        }
        if (threads != null) {
            boolean flip = false;
            for (long id : threads) {
                if (ids != null && !ArrayUtil.containsInLongArray(ids, id)) {
                    // we have an id constraint and we dont have that id in the
                    // constraint listFiles, so lets skip this thread.
                    continue;
                }
                if (flip = !flip) {
                    response.setRenderingRow(redLine);
                }
                ThreadInfo ti = threadMXBean.getThreadInfo(id, stackDepth);
                StackTraceElement elements[] = ti.getStackTrace();
                com.hitorro.util.commandandcontrol.MultiRowResponse mrr = response.getMultiRowResponse();
                mrr.addTuple(1,
                        ti.getThreadId(),
                        ti.getThreadState().toString(),
                        threadMXBean.getThreadCpuTime(id),
                        ti.getLockName(),
                        ti.getLockOwnerId(),
                        ti.getBlockedCount(),
                        ti.getBlockedTime(),
                        ti.getWaitedCount(),
                        ti.getWaitedTime());

                if (stackDepth > 0) {
                    mrr.add(0, ti.getThreadName());
                    for (int i = 0; i < elements.length; i++) {
                        mrr.addTuple(0, stackFrameRow(elements, i));
                    }
                }
                response.addMultiRowResponse(mrr);
            }
        }

        response.end();
        return true;
    }

    private void listAllThreadsSet(com.hitorro.util.commandandcontrol.Response set) {
        set.setResponseShape(headerInfo);
        ThreadGroup curr_thread_group;
        ThreadGroup root_thread_group;
        ThreadGroup parent;
        curr_thread_group = Thread.currentThread().getThreadGroup();
        root_thread_group = curr_thread_group;
        parent = root_thread_group.getParent();
        while (parent != null) {
            root_thread_group = parent;
            parent = parent.getParent();
        }
        listThreadGroupSet(set, "", root_thread_group);
    }

    private boolean printThreadInfoSet(com.hitorro.util.commandandcontrol.Response set, String parentGroup, Thread t) {
        if (t == null) {
            return false;
        }

        set.addRow("Thread", parentGroup, getThreadString(t), t.getId(), Constants.getInteger(t.getPriority()),
                t.isDaemon() ? "Yes" : "No", t.isAlive() ? "Yes" : "No");

        return true;
    }

    private String getThreadString(Thread t) {
        String name = null;
        if (t instanceof HTThread) {
            name = ((HTThread) t).getDescription();
        } else {
            name = t.getName();
        }
        return name;
    }

    private boolean listThreadGroupSet(com.hitorro.util.commandandcontrol.Response set, String parentGroup, ThreadGroup g) {
        if (g == null) {
            return false;
        }
        int num_threads = g.activeCount();
        int num_groups = g.activeGroupCount();
        Thread[] threads = new Thread[num_threads];
        ThreadGroup[] groups = new ThreadGroup[num_groups];
        g.enumerate(threads, false);
        g.enumerate(groups, false);
        String thisGroupsName = g.getName();
        set.addRow("Group", parentGroup, thisGroupsName, "N/A", "N/A", g.isDaemon() ? "Yes" : "No", "N/A");

        for (int i = 0; i < num_threads; i++) {
            printThreadInfoSet(set, parentGroup, threads[i]);
        }

        for (int i = 0; i < num_groups; i++) {
            listThreadGroupSet(set, thisGroupsName, groups[i]);
        }
        return true;
    }
}