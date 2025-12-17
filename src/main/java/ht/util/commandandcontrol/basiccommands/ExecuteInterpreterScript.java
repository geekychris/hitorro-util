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
package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.basefile.fs.BaseFile;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.interpreters.BaseInterpreter;
import ht.util.interpreters.InterpreterException;
import ht.util.json.keys.BasefileProperty;
import ht.util.json.keys.StringProperty;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 */
@CommandDef(command = "interpreter.script", description = "execute a script from one of the registered interpreters")
public class ExecuteInterpreterScript extends Command {
    @CommandArgument(required = true)
    public final BasefileProperty Script = new BasefileProperty("script", "Script to executed");

    @CommandArgument(required = true)
    public final StringProperty Var = new StringProperty("assignto", "assign to a variable", null);

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        BaseFile scr = Script.apply(args);

        String var = Var.apply(args);

        if (scr.exists()) {
            try {
                BaseInterpreter bi = BaseInterpreter.getInterpreterForFileExtension(scr);
                if (bi == null) {
                    this.writeSimpleError(response, "Could not find interpreter %s", scr);
                    return false;
                }
                Object o = bi.execute(scr, false);
                if (o != null) {
                    session.setVar(var, o);
                }
                this.writeSuccess(response, "returned %s", o);
            } catch (MalformedURLException e) {
                this.writeSimpleError(response, "Unable to process %s %e", e, e);
            } catch (IOException e) {
                this.writeSimpleError(response, "Unable to process %s %e", e, e);
            } catch (InterpreterException e) {
                this.writeSimpleError(response, "Unable to process %s %e", e, e);
            }
        }

        return true;
    }
}