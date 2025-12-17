package ht.jsontypesystem.executors;

import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.util.json.keys.propaccess.Propaccess;

public class IndexerFactory implements ExecutorFactory<IndexerAction> {
    @Override
    public IndexerAction getNew(final Field field, Group group, final Propaccess path) {
        return new IndexerAction(field, group, path);
    }
}
