package ht.jsontypesystem;

import ht.util.json.keys.propaccess.Propaccess;

public interface TypeVisitor<E extends TypeVisitor> {
    void enterType(Type type, Propaccess path);

    void leaveType(Type type, Propaccess path);

    boolean enterField(Field type, Propaccess path);

    void leaveField(Field type, Propaccess path);

    void enterGroup(final Field field, Group group, final Propaccess path);

    void leaveGroup(final Field field, Group group, final Propaccess path);
}
