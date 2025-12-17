package ht.util.core.iterator.database;

import ht.util.core.iterator.mappers.BaseMapper;

import java.sql.ResultSet;

public abstract class JDBCFieldFetcherValueAdapter<E> extends BaseMapper<ResultSet, E> {
    public abstract E apply(ResultSet record);

    public abstract void close();
}
