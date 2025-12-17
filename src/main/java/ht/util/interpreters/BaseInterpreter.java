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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class BaseInterpreter<T extends BaseInterpreter> {
    private static Map<String, BaseInterpreter> interpreter = getInterpreters();

    private static HashMap<String, BaseInterpreter> getInterpreters() {
        HashMap<String, BaseInterpreter> i = new HashMap();
        add(new RubyInterpreter(), i);
        add(new BeanShellInterpreter(), i);
        return i;
    }

    public static BaseInterpreter getInterpreterForFileExtension(BaseFile bf) {
        String ext = bf.getFileExtension(true);
        return getForExtension(ext);
    }

    public static BaseInterpreter getForExtension(final String ext) {
        BaseInterpreter bi = interpreter.get(ext);
        if (bi == null) {
            return null;
        }
        return bi.getNewInstance();
    }

    public static void add(BaseInterpreter bi, Map<String, BaseInterpreter> in) {
        for (String ext : bi.getFileExtensions()) {
            in.put(ext.toLowerCase(), bi);
        }
    }

    public abstract Object eval(String evalme) throws InterpreterException;

    public abstract Object execute(BaseFile bf, boolean cache) throws IOException, InterpreterException;

    public abstract T getNewInstance();

    public abstract String[] getFileExtensions();
}
