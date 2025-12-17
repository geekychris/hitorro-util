package ht.jsontypesystem.executors;

import ht.jsontypesystem.TypeVisitor;
import ht.util.json.keys.propaccess.Propaccess;

public interface ExecutorAction<E extends TypeVisitor> {
    void project(ProjectionContext pc, Propaccess path, final boolean isMulti, final String lang);
}
