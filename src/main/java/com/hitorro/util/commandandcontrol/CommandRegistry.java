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
package com.hitorro.util.commandandcontrol;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.commandandcontrol.basiccommands.*;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassAnoUtil;
import com.hitorro.util.core.classes.MethodAnnotation;
import com.hitorro.util.core.classes.methodanoconstraints.MethodAnnotationMatches;
import com.hitorro.util.core.classes.parameteranoconstraint.ParamAnoClassMatch;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.trie.NonUniqueKeyException;
import com.hitorro.util.core.trie.Trie;
import com.hitorro.util.html.DumpHtmlStressResults;
import com.hitorro.util.html.RunHTMLStress;
import com.hitorro.util.io.resourcecache.file.DumpResourceCache;
import com.hitorro.util.json.keys.BaseMappingProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.ListTestUnits;
import com.hitorro.util.testframework.RunUnitTest;
import com.hitorro.util.testframework.RunUnitTestSuite;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/*
 *
 * User: chris
 */
public class CommandRegistry {

    private static CommandRegistry s_Command_registry = null;
    private static ResponseShape s_krdList = initKRDList();
    private static ResponseShape s_cdList = initCDList();
    private Map<String, Command> m_map = new TreeMap<String, Command>();

    public synchronized static final CommandRegistry getRegistry() {
        if (s_Command_registry == null) {
            s_Command_registry = new CommandRegistry();
            s_Command_registry.registerBasicVerbs();
        }
        return s_Command_registry;
    }

	public Collection<Command>getCommands () {
		return m_map.values();
	}
    private static List<Command> getDebugCommands(Object interogateMe, Class classToInterogate) {
        List<Command> commands = new ArrayList();

        if (classToInterogate == null) {
            classToInterogate = interogateMe.getClass();
        }

        MethodAnnotationMatches hasCommandDef = new MethodAnnotationMatches(CommandDef.class);
        List<MethodAnnotation> list = new ArrayList();
        ClassAnoUtil.getAllMemberFunctions(classToInterogate,
                hasCommandDef,
                AlwaysTrueOperator.oper,
                list);

        ParamAnoClassMatch paramMatch = new ParamAnoClassMatch(DebugArgAno.class);
        for (MethodAnnotation meth : list) {
            Annotation anos[][] = meth.getAnnotationForParametersMatching(paramMatch);
            DebugCommandArg pkeys[] = new DebugCommandArg[anos.length];
            for (int i = 0; i < anos.length; i++) {
                if (anos[i] == null) {
                    Console.println();
                }
                Annotation ano = anos[i][0];
                if (ano != null) {
                    DebugArgAno daa = (DebugArgAno) ano;
                    Class c = daa.propType();
                    try {
                        Constructor cons = c.getDeclaredConstructor(DebugArgAno.class);
                        pkeys[i] = new DebugCommandArg(daa.mustExist(), (BaseMappingProperty) cons.newInstance(daa), ((DebugArgAno) ano).argType());
                    } catch (NoSuchMethodException e) {
                        Log.commands.fatal("Unable to initialize FunctionCommand %s %e", e, e);
                    } catch (InvocationTargetException e) {
                        Log.commands.fatal("Unable to initialize FunctionCommand %s %e", e, e);
                    } catch (InstantiationException e) {
                        Log.commands.fatal("Unable to initialize FunctionCommand %s %e", e, e);
                    } catch (IllegalAccessException e) {
                        Log.commands.fatal("Unable to initialize FunctionCommand %s %e", e, e);
                    }
                }

            }
            //public FunctionCommand (Object o, Method method, CommandDef def, DebugCommandArg keys[])
            FunctionCommand fc = new FunctionCommand(interogateMe, meth.getMethod(), (CommandDef) meth.getAnnotation(CommandDef.class), pkeys);
            commands.add(fc);
        }
        return commands;
    }

    private static ResponseShape initKRDList() {
        ResponseShape krdList = new ResponseShape("commands", "command");
        krdList.addHeader("Key", "Required", "Description");
        krdList.addHeaderShortNames("key", "req", "desc");
        krdList.addRowTypes(String.class, Boolean.class, String.class);
        return krdList;
    }

    private static ResponseShape initCDList() {
        ResponseShape list = new ResponseShape("commands", "command");
        list.addHeader("Command", "Description");
        list.addHeaderShortNames("Command", "Description");
        list.addRowTypes(String.class, String.class);
        return list;
    }

    public static void list(Command command, Response response, CommandSession session) {
        response.setResponseShape(s_krdList);
        List<DebugCommandArg> args = command.getArguments();
        for (DebugCommandArg arg : args) {
            response.addRow(arg.getName(), arg.getRequired(), arg.getDescription());
        }
    }

    public static void listCommands(Response resp, CommandSession session) {
        Collection<Command> commandsCollections = getRegistry().m_map.values();
        Iterator<Command> iter = commandsCollections.iterator();
        resp.setResponseShape(s_cdList);
        while (iter.hasNext()) {
            Command command = iter.next();
            resp.addRow(command.getCommand(), command.getDescription());
        }
        resp.end();
    }

    public Trie getTrie() {
        Trie trie = new Trie();
        Set<String> set = m_map.keySet();
        for (String s : set) {
            try {
                trie.insert(s, m_map.get(s));
            } catch (NonUniqueKeyException e) {
                Log.util.error("Exception %s %e", e, e);
            }

        }
        return trie;
    }

    public Trie getTrieForArgs(String command) {
        Command com = this.get(command);
        if (com != null) {
            List<DebugCommandArg> args = com.getArguments();
            Trie trie = new Trie();
            for (DebugCommandArg arg : args) {
                try {
                    trie.insert(arg.getName(), arg);
                } catch (NonUniqueKeyException e) {
                    Log.util.error("Exception %s %e", e, e);
                }
            }
            return trie;
        }
        return null;
    }

    public void addAllFromObject(Object interogateMe) {
        try {
            List<Command> commands = getDebugCommands(interogateMe, null);
            this.addAll(commands);
        } catch (Exception e) {
            List<Command> commands = getDebugCommands(interogateMe, null);
            Console.println();
        }

    }

    public void addAllFromClass(Class interogateMe) {
        List<Command> commands = getDebugCommands(null, interogateMe);
        this.addAll(commands);
    }

    public void addAll(List<Command> commands) {
        if (commands == null) {
            return;
        }
        for (Command command : commands) {
            add(command);
        }
    }

    public void add(Command command) {
        if (command == null) {
            return;
        }
        if (command.getStandardInit()) {
            command.initAnos();
            command.init();
        }
        m_map.put(command.getCommand(), command);
    }

    public Command get(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return null;
        }
        return m_map.get(s);
    }

    public boolean execute(String rawArgs, String s,
                           JVS map,
                           Response response,
                           CommandSession session) throws PropaccessError {
        Command command = get(s);
        if (command == null) {
            response.addInfo(InfoLevel.Error, Fmt.S("Command %s not known", command));
            response.end();
            return false;
        }
        List<DebugCommandArg> args = command.getArguments();
        for (DebugCommandArg arg : args) {
            String val = map.getString(arg.getName());
            if (arg.isHidden()) {
                continue;
            }
            if (StringUtil.nullOrEmptyOrBlankString(val) && arg.getRequired()) {
                response.addInfo(InfoLevel.Error, Fmt.S("Key %s required but not provided", arg
                        .getName()));
                list(command, response, session);
                response.end();
                return false;
            } else if (!StringUtil.nullOrEmptyOrBlankString(val)) {
                String error = arg.validate(map);
                if (!StringUtil.nullOrEmptyOrBlankString(error)) {
                    response.addInfo(InfoLevel.Error, Fmt.S("Key %s is not valid with error %s", arg
                            .getName(), error));
                    list(command, response, session);
                    response.end();
                    return false;
                }
            }
        }
        try {
            boolean result = command.execute(rawArgs, map, response, session, RestOperations.Get);
            response.end();
            return result;
        } catch (IOException e) {
            response.addInfo(InfoLevel.Error, Fmt.S("Exception occured executing %s, %s %e",
                    command.getCommand(), e, e));
            response.end();
            return false;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // dont catch all exceptions let it fail!
        return true;
    }

    public final void registerBasicVerbs() {
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpCommands());
        add(new com.hitorro.util.commandandcontrol.basiccommands.Exit());
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpLogLevels());
        add(new com.hitorro.util.commandandcontrol.basiccommands.SetLogLevel());
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpThreads());
        add(new com.hitorro.util.commandandcontrol.basiccommands.Help());
        add(new RunUnitTest());
        add(new RunUnitTestSuite());
        add(new ListTestUnits());
        add(new com.hitorro.util.commandandcontrol.basiccommands.RunScript());
        add(new com.hitorro.util.commandandcontrol.basiccommands.EventCommand());
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpEventListeners());
        add(new com.hitorro.util.commandandcontrol.basiccommands.VersionDump());
        add(new RunHTMLStress());
        add(new DumpHtmlStressResults());
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpProcessStats());
        add(new DumpResourceCache());
        add(new com.hitorro.util.commandandcontrol.basiccommands.DumpDiskUsage());
        add(new com.hitorro.util.commandandcontrol.basiccommands.EnterInteractive());
        add(new com.hitorro.util.commandandcontrol.basiccommands.ExecuteInterpreterScript());
        add(new com.hitorro.util.commandandcontrol.basiccommands.SetConfigEntry());
        this.addAllFromClass(com.hitorro.util.commandandcontrol.basiccommands.BasicCommands.class);
    }
}
