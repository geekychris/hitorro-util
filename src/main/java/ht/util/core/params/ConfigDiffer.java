package ht.util.core.params;

import ht.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: chris
 */
public class ConfigDiffer {
    Iterator<String> oldIter;
    Iterator<String> newIter;
    String oldKey;
    String newKey;
    boolean of = true;
    boolean nf = true;
    List<ConfigChange> changes = new ArrayList();

    public Iterator<ConfigChange> getDiffs(HTProperties old, HTProperties newC) {
        changes.clear();
        oldIter = old.getMap().keySet().iterator();
        newIter = newC.getMap().keySet().iterator();

        advanceOld();
        advanceNew();
        while (of && nf) {
            int c = oldKey.compareTo(newKey);
            if (c == 0) {
                // equal key, ensure same values
                String oldVals = old.get(oldKey);
                String newVals = newC.get(newKey);
                if (!StringUtil.equals(oldVals, newVals, false)) {
                    changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Updated, oldKey));
                }

                advanceOld();
                advanceNew();
            } else if (c < 0) {
                changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, oldKey));
                advanceOld();
            } else {
                changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, newKey));
                advanceNew();
            }

        }

        while (of) {
            changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, oldKey));
            advanceOld();
        }

        while (nf) {
            changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, newKey));
            advanceNew();
        }

        return changes.iterator();
    }

    private void advanceOld() {
        if (of == false) {
            return;
        }
        if (oldIter.hasNext()) {
            oldKey = oldIter.next();
            of = !StringUtil.nullOrEmptyString(oldKey);
        } else {
            of = false;
        }
    }

    private void advanceNew() {
        if (nf == false) {
            return;
        }
        if (newIter.hasNext()) {
            newKey = newIter.next();
            nf = !StringUtil.nullOrEmptyString(newKey);
        } else {
            nf = false;
        }
    }


}
