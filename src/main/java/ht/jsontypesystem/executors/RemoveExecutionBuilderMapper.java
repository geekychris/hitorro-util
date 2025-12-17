package ht.jsontypesystem.executors;

import ht.jsontypesystem.BaseT;
import ht.jsontypesystem.Type;
import ht.jsontypesystem.grouppredicates.GroupNameFilter;
import ht.util.core.iterator.mappers.BaseMapper;

import java.util.function.Predicate;

public class RemoveExecutionBuilderMapper extends BaseProjectionFactoryMapper<RemoveAction> {
    public static BaseMapper<Type, ExecutionBuilder> me = new EnrichExecutionBuilderMapper();

    public RemoveExecutionBuilderMapper() {
        // predicates based upon group is kinda wrong for removing

        predicate = (Predicate<BaseT>) GroupNameFilter.enrichFilter;
    }

    @Override
    public ExecutionBuilder<RemoveAction> getFactory() {
        return new ExecutionBuilder(new RemoveFactory());
    }
}
