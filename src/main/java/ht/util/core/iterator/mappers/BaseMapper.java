package ht.util.core.iterator.mappers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public abstract class BaseMapper<I, O> implements Mapper<I, O> {
    public boolean isThreadSafe() {
        return true;
    }

    /**
     * overide this function if you require cleanup
     */
    public void close() {

    }

    public BaseMapper getCopy() {
        return this;
    }

    public Class inputType() {
        return Object.class;
    }

    public Class outputType() {
        return Object.class;
    }

    public String getDescription() {
        return "description not defined";
    }

    public <E> BaseMapper<I, E> combine(BaseMapper<O, E> mapper) {
        if (mapper instanceof DummyBaseMapper) {
            //XXX Hack to see if I can work around a generics flaw I dont understand where HTSerializable cant seem to be use as a ? extends HTSerializable
            return (BaseMapper<I, E>) this;
        }
        return new MapperCollection(this, mapper);
    }


    public <E> BaseMapper<I, O> combineIterMapping(BaseMapper<AbstractIterator<O>, AbstractIterator<E>> iteratorMapper, BaseMapper<E, O> mapper) {
        return new IterMapMapper(mapper);
    }
}

class IterMapMapper<I, O> extends BaseMapper<AbstractIterator<I>, AbstractIterator<O>> {
    private BaseMapper<I, O> mapper;

    public IterMapMapper(BaseMapper<I, O> mapper) {
        this.mapper = mapper;
    }

    @Override
    public AbstractIterator<O> apply(final AbstractIterator<I> e) {
        return e.map(mapper);
    }
}
