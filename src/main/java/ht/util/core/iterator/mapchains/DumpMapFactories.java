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
package ht.util.core.iterator.mapchains;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringProperty;

/**
 *
 */

@CommandDef(command = "mapfactory.listFiles", description = "List the available apply factories and their underlying apply functions")
public class DumpMapFactories extends Command {
    @CommandArgument(required = false)
    public static final StringProperty Name = new StringProperty("mapname", "Name of the apply", null);

    private MapFactoryService service;
    @ResponseDefinition(command = "tests",
            rowname = "test",
            columns = {@RespColumn(name = "Key", lName = "key"),
                    @RespColumn(name = "Description", lName = "desc"),
                    @RespColumn(name = "Input Type", lName = "intype"),
                    @RespColumn(name = "Output Type", lName = "outtype")})
    private ResponseShape rs = new ResponseShape();

    public void setFromService(Object o) {
        this.service = (MapFactoryService) o;
    }


    @Override
    public boolean execute(final String rawValue, final JVS args, final Response response, final CommandSession session, RestOperations operation) throws Exception {
        String name = Name.apply(args);
        if (StringUtil.nullOrEmptyString(name)) {
            this.writeKeyValue(response, this.getKVShape(), service.getDescriptions());
        } else {
            MapFactory mf = service.getMapFactory(name);
            if (mf == null) {
                this.writeSimpleError(response, "factory %s is not known", name);
                return false;
            }
            response.setResponseShape(rs);
            mf.addResponseRow(response);
            response.end();
        }
        return true;
    }


}
