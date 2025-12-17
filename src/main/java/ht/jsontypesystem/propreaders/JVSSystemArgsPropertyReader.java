package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;
import ht.util.core.Env;


/**
 * Get the properties from the "environment" and put them to the config.
 */
public class JVSSystemArgsPropertyReader implements JVSPropertiesReader {
    public void getProperties(JVS props, JVS cmdLineArgs) {
        props.addMap(Env.getSystemArgs());
    }

    public boolean havePropertiesChanged() {
        return false;
    }
}
