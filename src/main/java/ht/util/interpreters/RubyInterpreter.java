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
package ht.util.interpreters;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BaseFileUtil;
import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * http://kenai.com/projects/jruby/pages/DirectJRubyEmbedding http://blogs.sun.com/coolstuff/entry/using_java_classes_in_jruby
 */
public class RubyInterpreter extends BaseInterpreter {
    private static Ruby runtime = null;

    private RubyRuntimeAdapter evaler = JavaEmbedUtils.newRuntimeAdapter();

    private Map<BaseFile, JavaEmbedUtils.EvalUnit> cacheMap = new HashMap();

    private synchronized static Ruby getRuntime() {
        if (runtime == null) {
            runtime = JavaEmbedUtils.initialize(new ArrayList());
        }
        return runtime;
    }

    public Object eval(String evalme) throws InterpreterException {
        IRubyObject object;
        try {
            object = evaler.eval(getRuntime(), evalme);
        } catch (RaiseException re) {
            throw new InterpreterException(re);
        }
        return object;
    }

    public Object execute(BaseFile bf, boolean cache) throws InterpreterException {
        try {
            JavaEmbedUtils.EvalUnit eu = evalUnit(bf, cache);
            return eu.run();
        } catch (RaiseException re) {
            throw new InterpreterException(re);
        }
    }

    @Override
    public BaseInterpreter getNewInstance() {
        return new RubyInterpreter();
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"rb"};
    }

    private JavaEmbedUtils.EvalUnit evalUnit(BaseFile bf, boolean cache) {
        JavaEmbedUtils.EvalUnit eu;
        if (cache) {
            eu = cacheMap.get(bf);
            if (eu != null) {
                return eu;
            }
        }
        eu = evalUnit(BaseFileUtil.bf2inputstream.apply(bf));
        if (cache && eu != null) {
            cacheMap.put(bf, eu);
        }
        return eu;
    }

    private JavaEmbedUtils.EvalUnit evalUnit(InputStream is) {
        return evaler.parse(getRuntime(), is, "", 0);
    }
}
