package ht.util.core.iterator.mappers;

/**
 *
 */
public class NullOperBaseMapper<I> extends BaseMapper<I, I> {
    public static final NullOperBaseMapper instance = new NullOperBaseMapper();

    @Override
    public I apply(final I e) {
        return e;
    }
}
