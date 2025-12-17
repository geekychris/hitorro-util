package ht.jsontypesystem.executors;

import ht.jsontypesystem.BaseT;
import ht.jsontypesystem.Type;
import ht.jsontypesystem.grouppredicates.GroupNameFilter;
import ht.util.core.iterator.mappers.BaseMapper;

import java.util.function.Predicate;

public
class EnrichExecutionBuilderMapper extends BaseProjectionFactoryMapper<EnrichAction> {
    public static final BaseMapper<Type, ExecutionBuilder> me = new EnrichExecutionBuilderMapper();
    public EnrichExecutionBuilderMapper() {
        predicate = (Predicate<BaseT>) GroupNameFilter.enrichFilter;
    }

    @Override
    public ExecutionBuilder<EnrichAction> getFactory() {
        return new ExecutionBuilder(new EnrichFactory());
    }
}