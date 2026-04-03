/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.iterator;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.mappers.MapToTuple;
import com.hitorro.util.core.iterator.mappers.SettableMapper;
import com.hitorro.util.core.iterator.reducers.ListReducer;
import com.hitorro.util.core.iterator.sinks.IteratorSink;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.servicecounters.CounterSet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An iterator that makes it easy to chain things together, such as filters and mapping
 */
public abstract class AbstractIterator<E> implements ChainingIteratorIntf<E>, Iterable<E>, AutoCloseable {
    public static void attemptClose(Iterator iter) throws Exception {
        if (iter instanceof AutoCloseable) {
            ((AutoCloseable) iter).close();
        }
    }

    public AbstractIterator<E> iterator() {
        return this;
    }

    public void addAll(List<E> list) {
        while (hasNext()) {
            list.add(next());
        }
    }

    public int devNull() {
        int count = 0;
        while (hasNext()) {
            next();
            count++;
        }
        return count;
    }

    public E getIfHasNext() {
        return getIfHasNext(null);
    }

    public E getIfHasNext(E defaultV) {
        if (hasNext()) {
            return next();
        }
        return defaultV;
    }

    public <F> F getIfHasNextMap(Function<E, F> function, E defaultValue) {
        E e = getIfHasNext(defaultValue);
        return function.apply(e);
    }

    /**
     * grab the first item and close the iterator
     *
     * @return
     * @throws IOException
     */
    public E getFirstItemAndClose() throws Exception {
        if (this.hasNext()) {
            E e = this.next();
            this.close();
            return e;
        }
        return null;
    }

    public E getFirstItem() {
        if (this.hasNext()) {
            E e = this.next();
            return e;
        }
        return null;
    }

    public <OUT> AbstractIterator<OUT> fillIterator(int batchSize, FillBufferHandler<E, OUT> transformer) {
        return new FillBufferIterator(this, batchSize, transformer);
    }

    public AbstractIterator<E> count(AtomicLong longV) {
        return new CountingIterator(this, longV, 0);
    }

    public AbstractIterator<E> count(AtomicLong longV, long reportEvery) {
        return new CountingIterator(this, longV, reportEvery);
    }

    /**
     * Sort the values of the input iterator in memory.  If the dataset is too big then the serialized merge sort will be required.
     *
     * @param comp
     * @return
     */
    public AbstractIterator<E> sort(Comparator<E> comp) {
        return new SortIterator(this, comp);
    }

    /**
     * @param other
     * @param <F>
     * @return
     */
    public <F> AbstractIterator<GenericKeyValue<E, F>> combine(AbstractIterator<F> other) {
        return new TupleIterator(this, other);
    }


    public <F> AbstractIterator<GenericKeyValue<E, F>> tupleMap(Function<E, F> function) {
        return map(new MapToTuple<E, F>(function));
    }

    /**
     * chain the iterator through a mapping function.
     *
     * @param mapper
     * @param <OUT>
     * @return
     */
    public <OUT> AbstractIterator<OUT> map(Function<E, OUT> mapper) {
        return new MappingIterator(this, mapper);
    }

    public AbstractIterator<E> skipNTakeM(long skip, long take, Predicate<E> excludeInTake, boolean expungeRemainder) {
        return new SkipNTakeM(this, skip, take, excludeInTake, expungeRemainder);
    }

    public AbstractIterator<E> skipNTakeM(long skip, long take, boolean expungeRemainder) {
        return new SkipNTakeM(this, skip, take, null, expungeRemainder);
    }

    /**
     * if a mapping function can output a null value this iterator will apply and remove those nulls.
     *
     * @param mapper
     * @param <O>
     * @return
     */
    public <O> AbstractIterator<O> mapRemoveNull(Mapper<E, O> mapper) {
        return new NullRemovingIterator(new MappingIterator<E, O>(this, mapper));
    }

    public <OUT> AbstractIterator<OUT> mapParallel(Function<E, OUT> mapper) {
        int cores = Env.getCPUCores();
        return mapParallel(mapper, cores, 100, 100, this.getClass().getName());
    }


    public <OUT> AbstractIterator<OUT> mapParallel(Function<E, OUT> mapper, int inputQueueSize, int outputQueueSize) {
        return new ParIterator(this, mapper, Env.getCPUCores(), inputQueueSize, outputQueueSize, this.getClass().getName());
    }

    public <OUT> AbstractIterator<OUT> mapParallel(Function<E, OUT> mapper, int threads, int inputQueueSize, int outputQueueSize, String name) {
        return new ParIterator(this, mapper, threads, inputQueueSize, outputQueueSize, name);
    }

    public <OUT> AbstractIterator<OUT> timeLimitingIterator(long timeLimitInMillis) {
        if (timeLimitInMillis > 0) {
            return new TimeLimitingIterator(timeLimitInMillis, this);
        }
        return (AbstractIterator<OUT>) this;
    }

    public <E> AbstractIterator<E> rateLimitingIterator(int rpm) {
        if (rpm > 0) {
            return new RateLimitingIterator(rpm, this);
        }
        return (AbstractIterator<E>) this;
    }

    /**
     * HTPredicate out objects that dont meet the logical operator criteria
     *
     * @param oper
     * @param <E>
     * @return
     */
    public <E> AbstractIterator<E> filter(Predicate<E> oper) {
        return new FilteringIterator(this, oper);
    }


    public <E> AbstractIterator<E> filter(Predicate<E> oper, Sink<E> sink) {
        return new FilterToSinkIterator(this, oper, sink);
    }

    /**
     * Take all the
     *
     * @param sinkOutput
     * @throws IOException
     */
    public void sink(IteratorSink<E> sinkOutput) throws IOException {
        sinkOutput.setIterator(this);
        sinkOutput.sink();
    }

    public int sink(Sink<E> sink) throws IOException {
        return sink(sink, true, true);
    }

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
    public int sink(Sink<E> sink, boolean start, boolean stop) throws IOException {
        int counter = 0;
        if (start) {
            sink.start();
        }
        while (this.hasNext()) {
            try {
                sink.add(this.next());
            } catch (StoreException e) {
                throw new IOException(e);
            }
            counter++;
        }
        if (stop) {
            sink.stop();
        }
        return counter;
    }

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
    public <L, O> int sinkWithSettable(Sink<O> sink,
                                       BaseMapper<E, AbstractIterator<O>> mapper,
                                       SettableMapper<E, O, O> settingMapper) throws IOException {
        boolean first = true;
        int count = 0;
        while (hasNext()) {
            E e = next();
            settingMapper.setElem(e);

            count += mapper.apply(e).map(settingMapper).sink(sink, first, false);
            first = false;
        }
        sink.stop();
        return count;
    }

    /**
     * Place into the collection the contents of the iterator
     *
     * @param collection
     * @param limit      number of objects to put or -1 if you want the iterator to be expunged.
     * @return the collection
     */
    public List<E> toCollection(List<E> collection, int limit) {
        int count = 0;
        while (this.hasNext() && (limit > 0 || limit == -1)) {
            collection.add(this.next());
            count++;
            if (limit != -1) {
                limit--;
            }
        }
        return collection;
    }

    /**
     * transform the iterator stream into a map.
     *
     * @param map
     * @param limit number of objects to put or -1 if you want the iterator to be expunged.
     * @return the collection
     */
    public <K> Map<K, E> toMap(Map<K, E> map, Function<E, K> keyMapper, BinaryOperator<E> reducer, int limit) {
        int count = 0;
        while (this.hasNext() && (limit > 0 || limit == -1)) {
            E e = this.next();

            K k = keyMapper.apply(e);
            if (reducer != null) {
                E old = map.get(k);
                if (old != null) {
                    e = reducer.apply(old, e);
                }
            }
            map.put(k, e);
            count++;
            if (limit != -1) {
                limit--;
            }
        }
        return map;
    }


    public List<E> toCollection(List<E> collection) {
        return toCollection(collection, -1);
    }

    /**
     * fill the array with the maximum amount of items, sort the elements and optionally remove duplicates.
     *
     * @param array
     * @param start
     * @param max
     * @param merger
     * @param comparator
     * @return last index position of elements in the array after filling and merging.
     */
    public int toSortedArray(E[] array, int start, int max, LikeRowMerger<E> merger, Comparator<E> comparator) {
        int i = toArray(array, start, max);
        return sortIt(array, start, start + i, comparator, merger);
    }

    private int sortIt(E[] array, int start, int count, Comparator<E> comparator, LikeRowMerger<E> merger) {
        Arrays.sort(array, start, start + count, comparator);
        if (merger != null) {
            return removeDuplicates(array, comparator, merger, start, count);
        }
        return start + count;
    }

    private final int removeDuplicates(E[] arr, Comparator<E> comparator, LikeRowMerger<E> merger, int start, int size) {
        int insertInd = 0;
        int dedupe = 0;
        int end = start + size;
        for (int i = 1; i < end; i++) {
            E older = arr[insertInd];
            if (comparator.compare(arr[i], older) == 0) {
                arr[insertInd] = merger.apply(older, arr[i]);
                dedupe++;
            } else {
                arr[++insertInd] = arr[i];
            }
        }

        int newIndex = insertInd + 1;
        return newIndex;
    }

    /**
     * Copy content to array.
     *
     * @param array
     * @param start
     * @param max
     * @return number of elements added to array
     */
    public int toArray(E[] array, int start, int max) {
        int aSize = array.length;
        int iters = Math.min(aSize - start, max);
        int count = 0;
        while (this.hasNext() && iters > 0) {
            array[start + count] = this.next();
            count++;
            iters--;
        }
        return count;
    }

    /**
     * A windowed put.  Throw away the first few docs until we hit startIndex and then only process n items.
     *
     * @param sink
     * @param startIndex
     * @param items
     * @return
     * @throws IOException
     */
    public int sink(Sink<E> sink, int startIndex, int items) throws IOException {
        int counter = 0;
        sink.start();
        while (this.hasNext()) {
            E ee = this.next();
            if (counter >= startIndex) {
                if (counter >= startIndex + items) {
                    sink.stop();
                    return items;
                }
                try {
                    sink.add(ee);
                } catch (StoreException e) {
                    throw new IOException(e);
                }

            }
            counter++;
        }
        sink.stop();
        return counter;
    }

    public boolean close(Iterator iterIn) throws Exception {
        if (iterIn instanceof AutoCloseable) {
            ((AutoCloseable) iterIn).close();
        }
        return true;
    }

    /**
     * Given some condition we combine rows together assuming the rows are in some kind of order.
     *
     * @param comparator
     * @param merger
     * @return
     */
    public AbstractIterator<E> removing(Comparator<E> comparator, BiFunction<E, E, E> merger) {
        if (merger == null || comparator == null) {
            return this;
        }
        LikeRowMergingIterator<E> outIter = new LikeRowMergingIterator(this, comparator, merger);
        return outIter;
    }

    /**
     * encapsulate one iterator inside another mapping the output of the first iterator to the appropriate iterator
     *
     * @param mapper
     * @param <O>
     * @return
     */
    public <O> AbstractIterator<O> nest(Mapper<E, AbstractIterator<O>> mapper) {
        return new NestingIterator(this, mapper, null);
    }

    public <O> AbstractIterator<O> time(CounterSet cs) {
        return new TimingIterator(cs, this);
    }

    /**
     * encapsulate one iterator inside another mapping the output of the first iterator to the appropriate iterator
     *
     * @param mapper
     * @param <O>
     * @return
     */
    public <O> AbstractIterator<O> nest(Mapper<E, AbstractIterator<O>> mapper, NestingIteratorErrorHandler<E, O> handler) {
        return new NestingIterator(this, mapper, handler);
    }


    /**
     * @deprecated Use {@link #toStream()} which properly closes the iterator when the stream closes.
     */
    @Deprecated
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Bridge to Java Streams. The returned stream closes this iterator when the stream is closed.
     * Use with try-with-resources:
     * <pre>
     * try (Stream&lt;E&gt; stream = iter.toStream()) {
     *     stream.filter(...).map(...).collect(...);
     * }
     * </pre>
     */
    public Stream<E> toStream() {
        return StreamSupport.stream(spliterator(), false)
                .onClose(() -> { try { close(); } catch (Exception e) { /* closing */ } });
    }

    /**
     * Bridge from Java Streams to AbstractIterator.
     * The returned iterator supports all chaining operations (map, filter, nest, sink).
     */
    public static <E> AbstractIterator<E> fromStream(Stream<E> stream) {
        return new Iterator2AbstractIterator<>(stream.iterator());
    }

    /**
     * Terminal operation using a Java Collector (Collectors.toList(), Collectors.joining(), etc.)
     */
    public <R, A> R collect(java.util.stream.Collector<? super E, A, R> collector) {
        A container = collector.supplier().get();
        var accumulator = collector.accumulator();
        while (hasNext()) {
            accumulator.accept(container, next());
        }
        return collector.finisher().apply(container);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
    }

    public void close() throws Exception {
        // iterators may implement close
    }


    public <X> X reduce(BaseReducer<E, X> reducer) {
        return reducer.reduce(this);
    }

    /**
     * Composite reducer that will first combine like rows together (using a sort).
     *
     * @param comparator
     * @param combiner
     * @param reducer
     * @param <X>
     * @return
     */
    public <X> X reduce(Comparator<E> comparator, BiFunction<E, E, E> combiner, BaseReducer<E, X> reducer) {
        List<E> arr = (List<E>) this.reduce(ListReducer.me);
        Collections.sort(arr, comparator);
        return new CollectionIterator<E>(arr).removing(comparator, combiner).reduce(reducer);
    }
}