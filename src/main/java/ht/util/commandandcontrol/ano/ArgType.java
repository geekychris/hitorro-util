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
