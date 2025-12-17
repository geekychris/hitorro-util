package ht.util.core.thread.farm;

import ht.util.core.iterator.mappers.BaseMapper;

import java.util.function.Function;

/**
 * Brain dead farm command that takes a Mapper to do its mapping.  This is a limiting command as it does not give access
 * to thread local information that can be leveraged in a multi threaded type situation.
 */
public class MapperFarmCommand<InputType, OutputType> extends FarmCommand<InputType, OutputType, BaseMapper<InputType, OutputType>> {
    private Function<InputType, OutputType> mapper;

    private BaseMapper<InputType, OutputType> baseMapper;
    private boolean isBaseMapper = false;

    public MapperFarmCommand(Function<InputType, OutputType> mapper) {
        this.mapper = mapper;
        if (mapper instanceof BaseMapper) {
            isBaseMapper = true;
            baseMapper = (BaseMapper<InputType, OutputType>) mapper;
        }
    }

    @Override
    public OutputType apply(final InputType inElement) {
        if (isBaseMapper) {
            if (baseMapper.isThreadSafe()) {
                return mapper.apply(inElement);
            } else {
                BaseMapper<InputType, OutputType> m = this.getThreadData();
                if (m == null) {
                    m = baseMapper.getCopy();
                    this.setThreadData(m);
                }
                return m.apply(inElement);
            }
        } else {
            return mapper.apply(inElement);
        }
    }

    public void deinit(FarmThread ft) {
        super.deinit(ft);
        if (isBaseMapper) {
            baseMapper.close();
        }
    }
}
