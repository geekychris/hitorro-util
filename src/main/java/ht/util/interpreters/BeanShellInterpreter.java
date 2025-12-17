package ht.util.interpreters;

import bsh.EvalError;
import bsh.Interpreter;
import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BaseFileUtil;

/**
 *
 */
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
