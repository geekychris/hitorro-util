package ht.util.core.params;

import java.util.ArrayList;
import java.util.List;

/**
 * User: chris
 */
public class ChangesContainer implements ConfigChangeObserver {
    List<ConfigChange> changes = new ArrayList();
    boolean finished = false;

    public void change(ConfigChange cc) {
        changes.add(cc);
    }

    public void finished() {
        finished = true;
    }
}