package ht.jsontypesystem.executors;

import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.util.json.keys.propaccess.Propaccess;

public class RemoveFactory implements ExecutorFactory<RemoveAction> {
    @Override
    public RemoveAction getNew(final Field field, Group group, final Propaccess path) {
        return new RemoveAction(field, group, path);
    }
}
