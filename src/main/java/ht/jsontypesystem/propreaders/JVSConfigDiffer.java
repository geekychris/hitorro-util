package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;
import ht.jsontypesystem.JVSFieldDiffer;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.core.params.ConfigChange;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;

public class JVSConfigDiffer extends JVSFieldDiffer {
    private List<ConfigChange> changes = new ArrayList();

    public JVSConfigDiffer(final JVS jvs1, final JVS jvs2) {
        super(jvs1, jvs2);
    }


    @Override
    public void added(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Added, path.toString()));
    }

    @Override
    public void removed(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Deleted, path.toString()));
    }

    @Override
    public void changed(final Propaccess path) {
        changes.add(new ConfigChange(ConfigChange.ConfigChangeType.Updated, path.toString()));
    }

    public AbstractIterator<ConfigChange> getDiffs() {
        changes.clear();
        executeDiff();
        return new CollectionIterator<ConfigChange>(changes);
    }

}
