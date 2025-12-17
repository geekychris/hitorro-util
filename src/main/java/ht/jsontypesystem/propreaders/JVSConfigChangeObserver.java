package ht.jsontypesystem.propreaders;

import ht.util.core.params.ConfigChange;

public interface JVSConfigChangeObserver {
    void change(ConfigChange cc);

    void finished();
}