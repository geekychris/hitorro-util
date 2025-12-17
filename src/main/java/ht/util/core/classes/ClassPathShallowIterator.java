package ht.util.core.classes;

import ht.util.core.iterator.CollectionIterator;
import ht.util.core.string.StringUtil;

import java.io.File;

public class ClassPathShallowIterator extends CollectionIterator<String> {
    public ClassPathShallowIterator() {
        super(StringUtil.tokensFromSingleCharToList(System.getProperty("java.class.path"), File.pathSeparator));
    }

}



