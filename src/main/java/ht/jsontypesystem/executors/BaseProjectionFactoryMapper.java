package ht.jsontypesystem.executors;

import ht.jsontypesystem.BaseT;
import ht.jsontypesystem.Type;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.function.Predicate;

public abstract class BaseProjectionFactoryMapper<ACTION extends ExecutorAction> extends BaseMapper<Type, ExecutionBuilder> {
    protected Predicate<BaseT> predicate;

    @Override
    public ExecutionBuilder apply(final Type type) {
        Propaccess path = new Propaccess("");
        ExecutionBuilder<ACTION> mtv = getFactory();
        type.visit(mtv, predicate, path);
        mtv.finalizeNode();
        return mtv;
    }

    public Predicate<BaseT> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<BaseT> predicate) {
        this.predicate = predicate;
    }

    public abstract ExecutionBuilder<ACTION> getFactory();
}
