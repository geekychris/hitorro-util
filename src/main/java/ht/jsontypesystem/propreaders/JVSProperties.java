package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;

public class JVSProperties {
    private static JVS defaultProperties = null;

    public synchronized static void setDefaultProperties(JVS props, boolean runDiff) {
        if (runDiff) {
            JVSConfigChangeRegistry.getRegistry().runDiff(defaultProperties, props);
        }
        defaultProperties = props;
    }

    public static final String resolveJsonVariable(String value) {
        return defaultProperties.resolveJsonVariable(value);
    }

    public static JVS getProperties() {
        return defaultProperties;
    }
}
