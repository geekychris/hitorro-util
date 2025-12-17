package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringProperty;

/**
 *
 */
@CommandDef(command = "interactive", description = "Enter interactive input with a command that supports interactive commands")
public class EnterInteractive extends Command {
    @CommandArgument(required = true)
    public static final StringProperty cmd = new StringProperty("command", "command to interact with", null);

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String c = cmd.apply(args);
        Command command = CommandRegistry.getRegistry().get(c);
        if (command == null) {
            this.writeSimpleError(response, "Command %s was not found", c);
            return false;
        }
        if (StringUtil.nullOrEmptyOrBlankString(command.interactiveArgument())) {
            this.writeSimpleError(response, "Command %s is not setup for interactive input", c);
            return false;
        }
        session.setInteractiveCommand(command);
        this.writeSuccess(response, "Command set, enter \"exit\" to exit interactive mode");
        return true;
    }
}
