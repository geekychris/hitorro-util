package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.log.Logger;

@CommandDef(command = "env.loglevels", description = "Dump the logging levels defined in the system.")
public class DumpLogLevels extends Command {
    @ResponseDefinition(command = "loglevels",
            rowname = "category",
            columns = {@RespColumn(name = "Category", lName = "catname"),
                    @RespColumn(name = "Levle", lName = "level"),
                    @RespColumn(name = "Inherits from", lName = "from")})
    private ResponseShape header = new ResponseShape();

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        Logger.getLogLevels(response, header);
        return false;
    }

}
