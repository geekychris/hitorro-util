package ht.util.commandandcontrol;

import ht.jsontypesystem.JVS;
import ht.util.core.CommandArgs;
import ht.util.core.Log;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.propaccess.PropaccessError;

import java.text.ParseException;

/**
 *
 */
public class CommandMap {
    private String m_command;
    private JVS m_map;
    private String m_rawArgs;

    public CommandMap(String row) {
        int index = row.indexOf(" ");
        m_map = new JVS();
        if (index == -1) {
            m_command = row;
            m_rawArgs = "";
        } else {
            m_command = row.substring(0, index);
            m_rawArgs = row.substring(index + 1);
            try {
                CommandArgs.parseArgs(m_rawArgs, true, true, m_map);
            } catch (ParseException e) {
                Log.commands.error("Unable to parse command %s %e", e, e);
            } catch (PropaccessError e) {
                Log.commands.error("Unable to parse command %s %e", e, e);
            }
        }
    }

    public CommandMap(String cmd, JVS args, String rawArgs) {
        m_command = cmd;
        m_map = args;
        if (StringUtil.nullOrEmptyString(rawArgs)) {
            m_rawArgs = "";
        } else {
            m_rawArgs = rawArgs;
        }
    }

    public String getCommand() {
        return m_command;
    }

    public String getRawArgs() {
        return m_rawArgs;
    }

    public JVS getArgs() {
        return m_map;
    }

    public String toString() {
        return Fmt.S("%s %s", m_command, m_rawArgs);
    }
}
