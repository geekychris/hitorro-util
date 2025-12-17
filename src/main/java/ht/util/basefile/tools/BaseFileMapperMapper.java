package ht.util.basefile.tools;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.jarfile.JarItemIterator;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper to get the chaining iterator that is appropriate to the file extension of the base file
 * <p/>
 * Subclass this guy to provide the chainingiterator and its file extension
 */
public abstract class BaseFileMapperMapper<E> extends BaseMapper<BaseFile, AbstractIterator<E>> {
    private Map<String, BaseMapper<BaseFile, AbstractIterator<E>>> map = new HashMap();
    private boolean followJar;

    public BaseFileMapperMapper(boolean followJar) {
        this.followJar = followJar;
        BaseMapper<BaseFile, AbstractIterator<E>> m[] = getMappers();
        String ext[] = getExtensions();

        for (int i = 0; i < m.length; i++) {
            map.put(ext[i], m[i]);
        }
    }

    public abstract BaseMapper<BaseFile, AbstractIterator<E>>[] getMappers();

    public abstract String[] getExtensions();

    @Override
    public AbstractIterator<E> apply(final BaseFile e) {
        String ext = e.getFileExtension(true);
        if (StringUtil.nullOrEmptyString(ext)) {
            return null;
        }
        ext = ext.toLowerCase();
        if ("jar".equalsIgnoreCase(ext)) {
            return new JarItemIterator(e).nest(this);
        }
        BaseMapper<BaseFile, AbstractIterator<E>> mapper = map.get(ext);

        return mapper.apply(e);
    }
}
