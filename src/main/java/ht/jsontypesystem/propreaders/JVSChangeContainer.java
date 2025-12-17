package ht.jsontypesystem.propreaders;

import ht.util.core.params.ConfigChange;

import java.util.ArrayList;
import java.util.List;

public class JVSChangeContainer implements JVSConfigChangeObserver {
    List<ConfigChange> changes = new ArrayList();
    boolean finished = false;

    public void change(ConfigChange cc) {
        changes.add(cc);
    }

    public void finished() {
        finished = true;
    }
}