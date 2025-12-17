package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.LikeRowMerger;
import ht.util.core.iterator.Mapper;
import ht.util.core.iterator.sinks.Sink;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.function.Function;

/**
 *
 */
public class BaseFileAccessingObjectFactory<T> {
    protected Comparator<T> comparator;
    protected LikeRowMerger<T> merger = TakeRightRowMerger.me;
    protected boolean preferTreeBucketFlag = false;
    protected Class newArrayClass = Object.class;
    protected Function<Object, T> newFunc;
    private Mapper<BaseFile, Sink<T>> sinkMapper;
    private Mapper<BaseFile, AbstractIterator<T>> iteratorMapper;
    private T o;

    public T getObject() {
        if (this.o == null) {
            if (newFunc != null) {
                return newFunc.apply(null);
            }
        }
        T t = this.o;
        this.o = null;
        return t;
    }

    void returnObject(T l) {
        this.o = l;
    }

    public BaseFileAccessingObjectFactory setComparitor(Comparator<T> comp) {
        this.comparator = comp;
        return this;
    }

    public Comparator<T> getDefaultComparitor() {
        return comparator;
    }

    public BaseFileAccessingObjectFactory setNewArrayClass(Class newArrayClass) {
        this.newArrayClass = newArrayClass;
        return this;
    }

    public BaseFileAccessingObjectFactory setNewFunction(Function<Object, T> newFunc) {
        this.newFunc = newFunc;
        return this;
    }

    public BaseFileAccessingObjectFactory setMerger(LikeRowMerger<T> merger) {
        this.merger = merger;
        return this;
    }

    public LikeRowMerger<T> getRowMerger() {
        return merger;
    }

    /**
     * Anyone that has a factory used by the array bucket writer needs to implement something that returns a real array
     * of that type seems that generics is defeated here!
     *
     * @param i
     * @return
     */
    public T[] getArray(int i) {
        return (T[]) Array.newInstance(newArrayClass, i);
    }


    public BaseFileAccessingObjectFactory setPreferTreeBucket(boolean flag) {
        this.preferTreeBucketFlag = flag;
        return this;
    }

    public boolean preferTreeBucket() {
        return preferTreeBucketFlag;
    }

    public BaseFileAccessingObjectFactory setBaseFileSinkMapper(Mapper<BaseFile, Sink<T>> sinkMapper) {
        this.sinkMapper = sinkMapper;
        return this;
    }

    /**
     * How to write data out to a base file
     *
     * @return
     */
    public Mapper<BaseFile, Sink<T>> getBaseFileToSinkMapper() {
        return sinkMapper;
    }

    /**
     * How to read data from a base file to an iterator
     *
     * @return
     */
    public Mapper<BaseFile, AbstractIterator<T>> getBaseFileToChainingMapper() {
        return iteratorMapper;
    }

    public BaseFileAccessingObjectFactory setBaseFileToChainingMapper(Mapper<BaseFile, AbstractIterator<T>> iterMapper) {
        this.iteratorMapper = iterMapper;
        return this;
    }

    @Override
    public BaseFileAccessingObjectFactory clone() {
        BaseFileAccessingObjectFactory fac = new BaseFileAccessingObjectFactory();
        copyFields(fac);
        return fac;
    }

    public void copyFields(BaseFileAccessingObjectFactory obj) {
        obj.sinkMapper = this.sinkMapper;
        obj.iteratorMapper = this.iteratorMapper;
        obj.comparator = comparator;
        obj.merger = merger;
        obj.preferTreeBucketFlag = preferTreeBucketFlag;
        obj.newArrayClass = newArrayClass;
    }
}
