package ht.util.commandandcontrol;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.GenericKeyValue;
import ht.util.core.Log;
import ht.util.core.classes.ClassAnoUtil;
import ht.util.core.classes.MatchClass;
import ht.util.core.classes.MemberVarAnnotations;
import ht.util.core.classes.membervaranoconstraints.MethodVarAnoMatches;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.BaseMappingProperty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */

public abstract class Command {
    protected static final MethodVarAnoMatches hasCommandArg = new MethodVarAnoMatches(CommandArgument.class);
    protected static final MethodVarAnoMatches hasCommandResponseShape = new MethodVarAnoMatches(ResponseDefinition.class);
    protected static final MatchClass matchesDef = new MatchClass(CommandDef.class);
    protected CommandDef commandDef;
    protected List<DebugCommandArg> m_args = new ArrayList<DebugCommandArg>();
    private ResponseShape keyValShape;

    public static String initRequiredResponses(Object initMe) {
        List<MemberVarAnnotations> list = new ArrayList();
        ClassAnoUtil.getAllMemberVariable(initMe.getClass(), hasCommandResponseShape, list, true);
        for (MemberVarAnnotations mva : list) {
            try {
                mva.getField().setAccessible(true);
                Object o = mva.getField().get(initMe);
                if (o instanceof ResponseShape) {
                    ResponseDefinition rd = (ResponseDefinition) mva.getAnnotation(ResponseDefinition.class);
                    if (rd != null) {
                        ((ResponseShape) o).setFromDef(rd);
                    }
                } else {
                    return Fmt.S("Annotation was not associated with correct object type %s %s", o.getClass().getCanonicalName(), mva.getField().getName());
                }
            } catch (IllegalAccessException e) {
                Fmt.S("Unable to get arguments for class %s", initMe);
            }
        }
        return null;
    }

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        return execute(rawValue, args, response, session);
    }

    /**
     * Execute a command
     *
     * @param rawValue - non parsed version of the command line excluding the verb....usefull for custom parsing.
     * @param args
     * @param response
     * @return
     */
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session) throws Exception {
        return false;
    }

    /**
     * service that initialized this command.
     *
     * @param o
     */
    public void setFromService(Object o) {

    }

    public boolean getStandardInit() {
        return true;
    }

    public final String initAnos() {
        Class toInit = this.getClass();
        commandDef = (CommandDef) ClassAnoUtil.getClassLevelAnnotation(toInit, matchesDef);
        if (commandDef == null) {
            return Fmt.S("Unable to find command definition for command %s", this.getClass());
        }
        String err = initRequiredKeys(toInit);
        if (!StringUtil.nullOrEmptyString(err)) {
            return err;
        }

        err = initRequiredResponses(this);
        if (!StringUtil.nullOrEmptyString(err)) {
            return err;
        }
        return null;
    }

    /**
     * Init the headers
     */
    public void init() {

    }

    private String initRequiredKeys(Class toInit) {
        List<MemberVarAnnotations> list = new ArrayList();
        ClassAnoUtil.getAllMemberVariable(toInit, hasCommandArg, list);
        for (MemberVarAnnotations mva : list) {
            try {
                Object o = mva.getField().get(this);
                if (o instanceof BaseMappingProperty) {
                    CommandArgument ca = (CommandArgument) mva.getAnnotation(CommandArgument.class);
                    if (ca != null) {
                        DebugCommandArg dca = new DebugCommandArg(ca.required(), (BaseMappingProperty) o);
                        m_args.add(dca);
                    }
                }
            } catch (IllegalAccessException e) {
                Fmt.S("Unable to get arguments for class %s", toInit);
            }
        }
        return null;
    }

    // SOME COMMON HEADERS

    public String interactiveArgument() {
        return null;
    }

    protected ResponseShape getKVShape() {
        if (keyValShape == null) {
            keyValShape = new ResponseShape(getCommand(), "command");
            keyValShape.addHeader("Key", "Value");
            keyValShape.addHeaderShortNames("k", "v");
            keyValShape.addRowTypes(String.class, String.class);
        }
        return keyValShape;
    }

    public final boolean isInternal() {
        return commandDef.isInternal();
    }

    public boolean supportsRest() {
        return false;
    }

    public final RestOperations[] getRestOperations() {
        return commandDef.restOperations();
    }

    public boolean getSupportsOperation(RestOperations op) {
        for (RestOperations o : getRestOperations()) {
            if (o == op) {
                return true;
            }
        }
        return false;
    }

    /**
     * verb registered for invoking the command with.
     *
     * @return
     */
    public final String getCommand() {
        return commandDef.command();
    }


    /**
     * Description of command
     *
     * @return
     */
    public final String getDescription() {
        return commandDef.description();
    }

    public List<DebugCommandArg> getArguments() {
        return m_args;
    }


    /**
     * Execute a command
     *
     * @param rawValue - non parsed version of the command line excluding the verb....usefull for custom parsing.
     * @param args
     * @param response
     * @return
     */
    public long executeRestfull(String rawValue,
                                JVS args,
                                Response response,
                                CommandSession session,
                                RestOperations operation,
                                InputStream postPutInputStream) {
        switch (operation) {
            case Delete:
            case Put:
            case LastModified:
                return -1;
        }

        try {
            if (execute(rawValue, args, response, session, operation)) {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            Log.util.error("Exception executing %s with error %s, %e", rawValue, e, e);
        }
        return 0;
    }

    /**
     * Utility method to dump key values listFiles.
     *
     * @param list
     * @param shape
     * @param response
     */
    public void dumpKeyValue(List<GenericKeyValue<String, String>> list,
                             ResponseShape shape,
                             Response response) {
        response.setResponseShape(shape);
        for (GenericKeyValue kv : list) {
            response.addRow(kv.getKey(), kv.getValue());
        }
        response.end();
    }

    /**
     * Util method to put a key to the args listFiles
     *
     * @param required
     * @param key
     */
    protected void addArgument(boolean required, BaseMappingProperty key) {
        m_args.add(new DebugCommandArg(required, key));
    }

    /**
     * utility method for a simple error response.
     *
     * @param response
     * @param message
     * @param args
     */
    protected boolean writeSimpleError(Response response, String message, Object... args) {
        String m = Fmt.S(message, (Object[]) args);
        response.addInfo(InfoLevel.Error, m);
        response.end();
        return false;
    }

    protected boolean writeSimpleErrorRaw(Response response, String msg) {
        response.addRow(InfoLevel.Error, msg);
        response.end();
        return false;
    }

    /**
     * utility method for a simple informational message response.
     *
     * @param response
     * @param message
     * @param args
     */
    public boolean writeSuccess(Response response, String message, Object... args) {
        String m = Fmt.S(message, (Object[]) args);
        response.addInfo(InfoLevel.Info, m);
        response.end();
        return true;
    }

    /**
     * Simple two coloumn response writer.
     *
     * @param resp
     * @param kvs
     * @return true if successful.
     */
    protected boolean writeKeyValue(Response resp, ResponseShape shape, List<GenericKeyValue> kvs) {
        resp.setResponseShape(shape);
        for (GenericKeyValue kv : kvs) {
            resp.addRow(kv.getKey(), kv.getValue());
        }
        resp.end();
        return true;
    }

    protected boolean writeMap(Response resp, ResponseShape shape, Map<String, Object> kvs) {
        resp.setResponseShape(shape);
        Set<Map.Entry<String, Object>> set =
                kvs.entrySet();
        for (Map.Entry<String, Object> entry : set) {
            resp.addRow(entry.getKey(), entry.getValue());
        }
        resp.end();
        return true;
    }
}
