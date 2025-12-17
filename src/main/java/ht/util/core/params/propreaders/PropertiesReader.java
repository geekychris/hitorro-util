package ht.util.core.params.propreaders;

import ht.util.core.params.HTProperties;

import java.util.Map;

public interface PropertiesReader {
    void getProperties(HTProperties props, Map<String, String> cmdLineArgs);

    boolean havePropertiesChanged();
}
