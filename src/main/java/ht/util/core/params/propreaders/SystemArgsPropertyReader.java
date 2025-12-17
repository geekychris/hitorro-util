package ht.util.core.params.propreaders;

import ht.util.core.Env;
import ht.util.core.params.HTProperties;

import java.util.Map;


/**
 * Get the properties from the "environment" and put them to the config.
 */
public class SystemArgsPropertyReader implements PropertiesReader {
    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        props.readMap(Env.getSystemArgs(), null);
    }

    public boolean havePropertiesChanged() {
        return false;
    }
}
