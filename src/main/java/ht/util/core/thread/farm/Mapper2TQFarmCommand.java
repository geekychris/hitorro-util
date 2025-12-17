package ht.util.core.thread.farm;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;
import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 *
 */
public class Mapper2TQFarmCommand<InputType, IntermediateType, OutputType, ThreadDateType> extends FarmCommand<InputType, OutputType, ThreadDateType> {
    private Mapper<InputType, AbstractIterator<IntermediateType>> iterMapper;
    private Mapper<IntermediateType, OutputType> mapper;
    private AbstractEnqueue<OutputType> tq;
    private HTPredicate<OutputType> filter;

    public Mapper2TQFarmCommand(Mapper<InputType, AbstractIterator<IntermediateType>> iterMapper, Mapper<IntermediateType, OutputType> mapper,
                                HTPredicate<OutputType> filter, AbstractEnqueue<OutputType> tq) {
        this.iterMapper = iterMapper;
        this.mapper = mapper;
        this.tq = tq;
        this.filter = filter;
    }

    @Override
    public OutputType apply(final InputType inElement) {
        AbstractIterator<OutputType> iter = iterMapper.apply(inElement).map(mapper);
        if (filter != null) {
            iter = iter.filter(filter);
        }
        while (iter != null && iter.hasNext()) {
            try {
                tq.put(iter.next());
            } catch (InterruptedException e) {
            }
        }
        try {
            iter.close();
        } catch (IOException e) {

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}