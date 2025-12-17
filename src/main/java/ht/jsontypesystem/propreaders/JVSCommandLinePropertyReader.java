package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;

/**
 * Load properties from the command line
 */
public class JVSCommandLinePropertyReader implements JVSPropertiesReader {
    public void getProperties(JVS props, JVS cmdLineArgs) {
        props.merge(cmdLineArgs);
    }

    public boolean havePropertiesChanged() {
        return false;
    }
}
