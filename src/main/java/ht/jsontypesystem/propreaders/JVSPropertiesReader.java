package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;

public interface JVSPropertiesReader {
    void getProperties(JVS props, JVS cmdLineArgs) throws Exception;

    boolean havePropertiesChanged();
}
