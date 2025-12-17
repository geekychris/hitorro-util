package ht.jsontypesystem.executors;

import ht.jsontypesystem.BaseT;
import ht.jsontypesystem.Type;
import ht.jsontypesystem.grouppredicates.GroupNameFilter;
import ht.util.core.iterator.mappers.BaseMapper;

import java.util.function.Predicate;

public class IndexExecutionBuilderMapper extends BaseProjectionFactoryMapper<IndexerAction> {
    public static BaseMapper<Type, ExecutionBuilder> me = new IndexExecutionBuilderMapper();

    public IndexExecutionBuilderMapper() {
        predicate = (Predicate<BaseT>) GroupNameFilter.indexFilter;
    }

    @Override
    public ExecutionBuilder<IndexerAction> getFactory() {
        return new ExecutionBuilder(new IndexerFactory());
    }
}
