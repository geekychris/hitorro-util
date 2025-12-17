package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.cmdline.ConfigChangeWatcher;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.Env;
import ht.util.core.params.HTProperties;
import ht.util.io.FileUtil;
import ht.util.json.keys.StringProperty;

import java.io.*;

/**
 *
 */
@CommandDef(command = "env.setconfig", description = "Set a key in the configs")
public class SetConfigEntry extends Command {
    @CommandArgument(required = true)
    public static final StringProperty KeyProp = new StringProperty("key", "property key", null);
    @CommandArgument(required = true)
    public static final StringProperty ValueProp = new StringProperty("value", "property value", null);

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String key = KeyProp.apply(args);
        String value = ValueProp.apply(args);
        File f = Env.getSavedProps();
        HTProperties map = new HTProperties();
        if (FileUtil.notNullAndExists(f)) {

            InputStream is;
            try {
                is = FileUtil.getBufferedFileInputStream(f);
                HTProperties.load(is, map);
            } catch (FileNotFoundException e) {
                this.writeSimpleError(response, "Unable to read saved properties file %s %e", e, e);
                return false;
            } catch (IOException e) {
                this.writeSimpleError(response, "Unable to read saved properties file %s %e", e, e);
                return false;
            }

        }
        map.put(key.toLowerCase(), value);
        File tmp = FileUtil.getTempFileWithFromPeerFileWithExtension(f, "proptmp");
        OutputStream os = null;
        try {
            os = FileUtil.getBufferedFileOutputStream(tmp);
            map.write(os);
            FileUtil.swap(tmp, f);
            tmp.delete();
            // we saved, now lets force a load
            ConfigChangeWatcher.forceReload();
            this.writeSuccess(response, "Updated configs");
        } catch (FileNotFoundException e) {
            this.writeSimpleError(response, "Unable to write saved properties file %s %e", e, e);
            return false;
        }

        return false;
    }
}
