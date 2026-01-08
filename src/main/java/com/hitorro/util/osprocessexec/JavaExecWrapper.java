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
package com.hitorro.util.osprocessexec;

import com.hitorro.util.core.*;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.filefilters.FilenameExtensionFilter;
import com.hitorro.util.io.filefilters.OrCollection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * <p/>
 * The client compContext knows all about a service to run.  It knows:
 * <p/>
 * VM startup parameters classpath service startup parameters HT_BIN and HT_HOME roots of the application. If its
 * running, how long its been running, if it went down, how many times it went down
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * com.hitorro.util.cmdline.CommandLine command=com.hitorro.util.service.HTServer servertype=test level=full
 * test=ht.test.test.CoreUtilTest
 * <p/>
 * Mac: /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes
 */
public class JavaExecWrapper {
    private static final String Java = "java";
    private static final String Debug = "debug";
    private static final String Classpath = "classpath";
    private static final String ClassName = "class";
    private static final String Args = "args";
    private static final String JreParams = "jreparams";
    private static final FilenameFilter s_jarFilter = new OrCollection(new FilenameExtensionFilter("jar", true),
            new FilenameExtensionFilter("zip", true));
    File m_ptBinPath;
    File m_ptHomePath;
    File m_jreRoot;
    private String m_classPath;
    private boolean m_debugEnabled = false;
    private String m_port = "60000";
    private boolean m_suspend = false;
    private Map<String, String> m_args;
    private List<String> m_jreParams;
    private String m_class;
    private String m_command;


    public JavaExecWrapper(File binPath,
                           File homePath,
                           File jreRoot,
                           String className,
                           List<String> jreParams,
                           Map<String, String> args) {
        m_ptBinPath = binPath;
        m_ptHomePath = homePath;
        m_jreRoot = jreRoot;
        m_class = className;
        m_args = args;
        m_jreParams = jreParams;
    }


    /**
     * An exec that just waits for the process to complete.
     *
     * @param builder
     * @param error
     * @return
     * @throws Exception
     */
    public int simpleExec(StringBuilder builder, StringBuilder error) throws Exception {
        if (init() == false) {
            return -100;
        }
        return SimpleExec.exec(m_command, builder, error, true);
    }

    public String getExecString() throws IOException {
        if (init() == false) {
            return null;
        }
        return m_command;
    }

    public void setDebug(boolean enable, String port, boolean suspend) {
        m_suspend = suspend;
        m_port = port;
        m_debugEnabled = enable;
    }

    public boolean init() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        m_args.put("HT_BIN", m_ptBinPath.getCanonicalPath());
        m_args.put("HT_HOME", m_ptHomePath.getCanonicalPath());

        map.put(Java, com.hitorro.util.core.Platform.getPlatform().getJava(m_jreRoot).getCanonicalPath());
        map.put(Classpath, getClassPath());
        if (m_debugEnabled) {
            map.put(Debug, getDebugString(m_port, m_suspend));
        } else {
            map.put(Debug, "");
        }

        map.put(Args, com.hitorro.util.core.CommandArgs.getArgString(m_args));
        map.put(ClassName, m_class);
        map.put(JreParams, getJreParams());
        // ok I went this way so it was easier to read than a straight fmt.s
        m_command = Fmt.P("${java} ${jreparams} ${debug} -classpath ${classpath} ${class} ${args}", map);
        com.hitorro.util.core.Log.exec.debug("command to execute %s", m_command);

        return true;
    }

    private String getClassPath() throws IOException {
        StringBuilder b = new StringBuilder();
        com.hitorro.util.core.Platform.getPlatform().getJavaClassPath(b, m_jreRoot, s_jarFilter);
        // ht's third party
        ClassUtil.getExpandedClassPath(b, new File(m_ptBinPath, "lib"), s_jarFilter);
        b.append(com.hitorro.util.core.Env.getPathSeperator());
        b.append(new File(m_ptBinPath, "build").getCanonicalPath());
        return b.toString();
    }

    private String getDebugString(String port, boolean suspend) {
        return Fmt.S("-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%s",
                com.hitorro.util.core.BooleanUtil.getYNFlag(suspend),
                port);
    }

    private String getJreParams() {
        if (m_jreParams == null) {
            return "";
        }

        return StringUtil.mergeWithPrefixAndJoinToken(m_jreParams, "", " ");
    }
}
