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
