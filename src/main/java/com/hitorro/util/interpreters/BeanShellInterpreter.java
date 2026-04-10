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
package com.hitorro.util.interpreters;

import bsh.EvalError;
import bsh.Interpreter;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;


public class BeanShellInterpreter extends BaseInterpreter {
    protected Interpreter bshInterpreter = new Interpreter();

    @Override
    public Object eval(final String evalme) throws InterpreterException {
        try {
            return bshInterpreter.eval(evalme);
        } catch (EvalError evalError) {
            throw new InterpreterException(evalError);
        }
    }

    @Override
    public Object execute(final BaseFile bf, final boolean cache) throws InterpreterException {
        try {
            return bshInterpreter.eval(BaseFileUtil.bf2reader.apply(bf));  //To change body of implemented methods use File | Settings | File Templates.
        } catch (EvalError evalError) {
            throw new InterpreterException(evalError);
        }
    }

    @Override
    public BaseInterpreter getNewInstance() {
        return new BeanShellInterpreter();
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"bsh"};
    }
}
