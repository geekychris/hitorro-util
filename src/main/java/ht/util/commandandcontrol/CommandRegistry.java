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
package ht.util.commandandcontrol;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.commandandcontrol.basiccommands.*;
import ht.util.core.Console;
import ht.util.core.Log;
import ht.util.core.classes.ClassAnoUtil;
import ht.util.core.classes.MethodAnnotation;
import ht.util.core.classes.methodanoconstraints.MethodAnnotationMatches;
import ht.util.core.classes.parameteranoconstraint.ParamAnoClassMatch;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.core.trie.NonUniqueKeyException;
import ht.util.core.trie.Trie;
import ht.util.html.DumpHtmlStressResults;
import ht.util.html.RunHTMLStress;
import ht.util.io.resourcecache.file.DumpResourceCache;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.testframework.ListTestUnits;
import ht.util.testframework.RunUnitTest;
import ht.util.testframework.RunUnitTestSuite;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
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
        add(new DumpCommands());
        add(new Exit());
        add(new DumpLogLevels());
        add(new SetLogLevel());
        add(new DumpThreads());
        add(new Help());
        add(new RunUnitTest());
        add(new RunUnitTestSuite());
        add(new ListTestUnits());
        add(new RunScript());
        add(new EventCommand());
        add(new DumpEventListeners());
        add(new VersionDump());
        add(new RunHTMLStress());
        add(new DumpHtmlStressResults());
        add(new DumpProcessStats());
        add(new DumpResourceCache());
        add(new DumpDiskUsage());
        add(new EnterInteractive());
        add(new ExecuteInterpreterScript());
        add(new SetConfigEntry());
        this.addAllFromClass(BasicCommands.class);
    }
}
