package ht.util.core.params.propreaders;

import ht.util.core.params.HTProperties;

import java.util.Map;

/**
 * Load properties from the command line
 */
public class CommandLinePropertyReader implements PropertiesReader {
    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        props.readMap(cmdLineArgs, true, null);
    }

    public boolean havePropertiesChanged() {
        return false;
    }
}
