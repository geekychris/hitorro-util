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
package ht.util.commandandcontrol.basiccommands;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.*;
import ht.util.core.ArrayUtil;
import ht.util.core.Constants;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.core.thread.HTThread;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.StringProperty;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Dump all the threads running in the system.  Also is capable of dumping stack frames and
 */
@CommandDef(command = "env.threads", description = "Dump the threads running in the system and optionally their stack frames.")
public class DumpThreads extends Command {
    @CommandArgument(required = false)
    public static final BooleanProperty DumpStack = new BooleanProperty("stack", "Dump the thread stack", false);
    @CommandArgument(required = false)
    public static final BooleanProperty Desc = new BooleanProperty("description", "thread dump with groups and possible thread info", false);
    @CommandArgument(required = false)
    public static final BooleanProperty DumpDeadlock = new BooleanProperty("dumpdeadlock", "Dump deadlocked threads", false);
    @CommandArgument(required = false)
    public static final StringProperty Ids = new StringProperty("ids", "List of thread ids to constrain by", null);

    @ResponseDefinition(command = "dumpthreads",
            rowname = "thread",
            columns = {@RespColumn(name = "frame", lName = "frame"),
                    @RespColumn(name = "ID", lName = "id", type = Long.class),
                    @RespColumn(name = "State", lName = "state"),
                    @RespColumn(name = "CPUTime", lName = "cputime", type = Long.class),
                    @RespColumn(name = "LockName", lName = "lockName"),
                    @RespColumn(name = "LockOwnerId", lName = "lockOwnerId", type = Long.class),
                    @RespColumn(name = "BlockedCount", lName = "blockedCount"),
                    @RespColumn(name = "BlockedTime", lName = "blockedTime", type = Long.class),
                    @RespColumn(name = "WaitedCount", lName = "waitedCount", type = Long.class),
                    @RespColumn(name = "WaitedTime", lName = "WaitedTime", type = Long.class)},
            groups = {@ColumnGroup(start = 0, size = 1, name = "stack")})
    protected static ResponseShape header = new ResponseShape();

    @ResponseDefinition(command = "dumpthreads",
            rowname = "thread",
            columns = {@RespColumn(name = "Type", lName = "type"),
                    @RespColumn(name = "Parent", lName = "parent"),
                    @RespColumn(name = "Thread", lName = "thread"),
                    @RespColumn(name = "Id", lName = "id", type = Long.class),
                    @RespColumn(name = "Priority", lName = "priority"),
                    @RespColumn(name = "isDaemon", lName = "isdaemon"),
                    @RespColumn(name = "isAlive", lName = "isalive")})
    protected static ResponseShape headerInfo = new ResponseShape();

    RenderingContainer redLine[];

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
        redLine = new RenderingContainer[header.getHeaderLong().length];
        for (int i = 0; i < redLine.length; i++) {
            redLine[i] = new RenderingContainer(ColorEnum.blue, ColorEnum.red);
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
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
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
            response.addInfo(InfoLevel.Info, "No deadlocks found");
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
                MultiRowResponse mrr = response.getMultiRowResponse();
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

    private void listAllThreadsSet(Response set) {
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

    private boolean printThreadInfoSet(Response set, String parentGroup, Thread t) {
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

    private boolean listThreadGroupSet(Response set, String parentGroup, ThreadGroup g) {
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