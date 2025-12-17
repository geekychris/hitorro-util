package ht.jsontypesystem.executors;

import ht.jsontypesystem.Field;
import ht.jsontypesystem.Group;
import ht.util.json.keys.propaccess.Propaccess;

public class EnrichFactory implements ExecutorFactory<EnrichAction> {
    @Override
    public EnrichAction getNew(final Field field, Group group, final Propaccess path) {
        return new EnrichAction(field, group, path);
    }
}
