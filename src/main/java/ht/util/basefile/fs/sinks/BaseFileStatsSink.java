package ht.util.basefile.fs.sinks;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.sinks.BaseSink;


/**
 *
 */
public abstract class BaseFileStatsSink<E> extends BaseSink<E> {
    protected BaseFile outputFile;

    public BaseFileStatsSink(BaseFile outputFile) {
        this.outputFile = outputFile;
    }

    public void setBaseFile(BaseFile outFile) {
        this.outputFile = outFile;
    }
}
