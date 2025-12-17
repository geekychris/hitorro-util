package ht.util.core;

import ht.jsontypesystem.JVS;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.keys.propaccess.PropaccessError;

import java.text.ParseException;

/**
 *
 */
public class CommandArgsMapper extends BaseMapper<String, JVS> {
    public static final CommandArgsMapper instance = new CommandArgsMapper();

    @Override
    public JVS apply(final String s) {
        JVS jvs = new JVS();
        try {
            try {
                CommandArgs.getParameters(s, false, true, jvs);
            } catch (PropaccessError propaccessError) {
                return null;
            }
        } catch (ParseException e) {
            return null;
        }
        return jvs;
    }
}
