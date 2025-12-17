package ht.util.core.iterator;

import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.iterator.mappers.SettableMapper;
import ht.util.core.iterator.sinks.IteratorSink;
import ht.util.core.iterator.sinks.Sink;
import ht.util.servicecounters.CounterSet;

import java.io.IOException;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public interface ChainingIteratorIntf<E> extends CloseableIterator<E> {
    /**
     * Grabe the first item from
     *
     * @return
     * @throws IOException
     */
    E getFirstItemAndClose() throws Exception;

    /**
     * chain the iterator through a mapping function.
     *
     * @param mapper
     * @param <OUT>
     * @return
     */
    <OUT> AbstractIterator<OUT> map(Function<E, OUT> mapper);

    /**
     * HTPredicate out objects that dont meet the logical operator criteria
     *
     * @param oper
     * @param <E>
     * @return
     */
    <E> AbstractIterator<E> filter(Predicate<E> oper);

    /**
     * Take all the
     *
     * @param sinkOutput
     * @throws java.io.IOException
     */
    void sink(IteratorSink<E> sinkOutput) throws IOException;

    int sink(Sink<E> sink) throws IOException;

    /**
     * Sink this iterator to a sink, but we can control if we want to fire a start and end.  This allows us to nest
     * iterators for instance and drain the inner iterator of objects.  Usefull to handle marriage of data we have about
     * the outer iterator
     *
     * @param sink
     * @param start
     * @param stop
     * @return
     * @throws IOException
     */
    int sink(Sink<E> sink, boolean start, boolean stop) throws IOException;

    /**
     * Similar to what the nest achieves but if you must provide outer iterator type to the inner iterator then this is
     * the only current way to ensure you can provide that data.
     * <p/>
     * select e from E select o from O where id=e provide e->o
     *
     * @param sink
     * @param mapper
     * @param settingMapper
     * @param <L>
     * @param <O>
     * @return
     * @throws IOException
     */
    <L, O> int sinkWithSettable(Sink<O> sink,
                                BaseMapper<E, AbstractIterator<O>> mapper,
                                SettableMapper<E, O, O> settingMapper) throws IOException;

    /**
     * A windowed put.  Throw away the first few docs until we hit startIndex and then only process n items.
     *
     * @param sink
     * @param startIndex
     * @param items
     * @return
     * @throws IOException
     */
    int sink(Sink<E> sink, int startIndex, int items) throws IOException;


    /**
     * Given some condition we combine rows together assuming the rows are in some kind of order.
     *
     * @param comparator
     * @param merger
     * @return
     */
    AbstractIterator<E> removing(Comparator<E> comparator, BiFunction<E, E, E> merger);

    /**
     * encapsulate one iterator inside another mapping the output of the first iterator to the appropriate iterator
     *
     * @param mapper
     * @param <O>
     * @return
     */
    <O> AbstractIterator<O> nest(Mapper<E, AbstractIterator<O>> mapper);

    <O> AbstractIterator<O> time(CounterSet cs);

    /**
     * encapsulate one iterator inside another mapping the output of the first iterator to the appropriate iterator
     *
     * @param mapper
     * @param <O>
     * @return
     */
    <O> AbstractIterator<O> nest(Mapper<E, AbstractIterator<O>> mapper, NestingIteratorErrorHandler<E, O> handler);
}
