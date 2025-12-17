package ht.util.commandandcontrol.ano;

import ht.jsontypesystem.JVS;
import ht.jsontypesystem.JVS2JsonMapper;
import ht.jsontypesystem.Json2JVSMapper;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.DebugCommandArg;
import ht.util.commandandcontrol.RestOperations;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.sinks.JsonSink;
import ht.util.core.iterator.sinks.MappingSink;
import ht.util.io.FileUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public enum ArgType {

    Regular() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return key.getPropValue(jvs);
        }
    },
    Args() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return jvs;
        }
    },
    Raw() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return rawValue;
        }
    },
    Request() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return response.getHttpRequest();
        }
    },
    Uri() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return response.getHttpRequest().getRequestURI();
        }
    },
    Response() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return response;
        }
    },
    OutputStream() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            try {
                return response.getHttpResponse().getOutputStream();
            } catch (IOException e) {
                return null;
            }
        }

        public boolean surpressResponseWriting() {
            return true;
        }
    },
    Session() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            return session;
        }
    },
    JVSRequestIterator() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            if (response == null || response.getHttpRequest() == null) {
                return null;
            }
            try {
                AbstractIterator<JVS> iter = FileUtil.inputstream2JacksonjsonReader.apply(response.getHttpRequest().getInputStream()).map(Json2JVSMapper.me);
                return iter;
            } catch (IOException e) {
                return null;
            }
        }
    },
    JVSResponseSink() {
        public Object get(final String rawValue, final JVS jvs,
                          final ht.util.commandandcontrol.Response response, final CommandSession session,
                          DebugCommandArg key, final RestOperations operation) {
            if (response == null || response.getHttpRequest() == null) {
                return null;
            }
            try {

                OutputStream os = response.getHttpResponse().getOutputStream();
                JsonSink sink = new JsonSink(os);
                sink.setIndent(AnoUtils.indentProperty.apply(jvs));
                MappingSink ms = new MappingSink(sink, JVS2JsonMapper.me);

                return ms;
            } catch (IOException e) {
                return null;
            }
        }

        public boolean surpressResponseWriting() {
            return true;
        }
    };

    public abstract Object get(final String rawValue, final JVS jvs,
                               final ht.util.commandandcontrol.Response response,
                               final CommandSession session, DebugCommandArg key, final RestOperations operation);

    public boolean surpressResponseWriting() {
        return false;
    }
}
