package ht.util.commandandcontrol;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.ano.ArgType;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.sinks.JsonSink;
import ht.util.json.JSONUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class FunctionCommand extends Command {
    private Object o;
    private Method method;
    private DebugCommandArg keys[];
    private ResponseMapperInterface mapper;
    private ResponseShape responseShape;

    public FunctionCommand(Object o, Method method, CommandDef def, DebugCommandArg keys[]) {
        this.o = o;
        this.method = method;
        this.keys = keys;

        for (DebugCommandArg arg : keys) {
            if (!arg.isHidden()) {
                m_args.add(arg);
            }
        }
        this.commandDef = def;
        try {
            mapper = (ResponseMapperInterface) def.resultMapper().newInstance();
            // allow initialization of any response shape
            Command.initRequiredResponses(mapper);
            responseShape = mapper.getResponseShape().getCopy();
            responseShape.setTransaction(def.command());
        } catch (InstantiationException e) {
            Log.commands.fatal("Unable to get response mapping function for %s %s %e", def, e, e);
        } catch (IllegalAccessException e) {
            Log.commands.fatal("Unable to get response mapping function for %s %s %e", def, e, e);
        }
    }

    public boolean getStandardInit() {
        return false;
    }

    @Override
    public boolean execute(final String rawValue, final JVS jvs, final Response response, final CommandSession session, RestOperations operation) throws Exception {
        Object result;
        Object argsArray[] = new Object[keys.length];
        boolean writeResponse = true;
        for (int i = 0; i < keys.length; i++) {
            ArgType ag = keys[i].getArgType();
            argsArray[i] = ag.get(rawValue, jvs, response, session, keys[i], operation);
            if (ag.surpressResponseWriting()) {
                writeResponse = false;
            }
        }
        try {
            result = method.invoke(o, argsArray);
            if (writeResponse) {
                response.setResponseShape(responseShape);
                if (result instanceof Iterator) {
                    processIterator(response, (Iterator) result);
                } else if (result instanceof List) {
                    processList(response, (List) result);
                } else if (result.getClass().isArray()) {
                    processArray(response, (Object[]) result);
                } else if (result instanceof Map) {
                    processMap(response, (Map) result);
                } else if (result instanceof RedirectHttp) {
                    RedirectHttp r = (RedirectHttp) result;
                    response.getHttpResponse().sendRedirect(r.getPath());
                    return true;
                } else {
                    mapper.mapResponse(response, result);
                }
            }
            response.end();
            return true;
        } catch (IllegalAccessException e) {
            writeJsonError(response, JSONUtil.addCallstackChain(e));
            return false;
        } catch (InvocationTargetException e) {
            writeJsonError(response, JSONUtil.addCallstackChain(e));
            return false;
        }
        //this.writeSuccess(response, "Success: %s", result);
        // return true;
    }

    private void writeJsonError(final Response response, final JsonNode jsonNode) throws IOException {
        JsonNode json = jsonNode;
        response.getHttpResponse().setContentType("application/json");
        JsonSink sink = new JsonSink(response.getHttpResponse().getOutputStream());
        sink.add(json);
        sink.close();
    }

    private void processMap(final Response response, final Map result) {
        Map<Object, Object> map = result;
        Set<Map.Entry<Object, Object>> set = map.entrySet();
        Iterator<Map.Entry<Object, Object>> iter = set.iterator();
        while (iter.hasNext()) {
            mapper.mapResponse(response, iter.next());
        }
    }

    private void processJson(final Response response, final JsonNode result) {
        mapper.mapResponse(response, result);
    }


    private void processArray(final Response response, final Object[] result) {
        Object arr[] = result;
        for (Object o : arr) {
            mapper.mapResponse(response, o);
        }
    }

    private void processList(final Response response, final List result) {
        List list = result;
        for (Object o : list) {
            mapper.mapResponse(response, o);
        }
    }

    private void processIterator(final Response response, final Iterator result) {
        Iterator iter = result;
        while (iter.hasNext()) {
            mapper.mapResponse(response, iter.next());
        }
        try {
            if (iter instanceof AbstractIterator) {
                ((AbstractIterator) iter).close(iter);
            }
        } catch (Exception e) {
            Log.iterator.error("Unable to close iterator %s %e", e, e);
        }
    }
}
