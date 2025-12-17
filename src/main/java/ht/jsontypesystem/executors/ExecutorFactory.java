package ht.jsontypesystem.executors;

import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.util.json.keys.propaccess.Propaccess;

public interface ExecutorFactory<E extends ExecutorAction> {
    E getNew(final Field field, Group group, final Propaccess path);
}
