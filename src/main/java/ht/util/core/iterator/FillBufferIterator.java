package ht.util.core.iterator;

import ht.util.core.GenericKeyValue;
import ht.util.core.Log;


public class FillBufferIterator<I, O> extends AbstractIterator<O> {
    private AbstractIterator<I> iter;
    private FillState state = FillState.Init;
    private GenericKeyValue<I, O> buffer[];
    private int batchSize;
    private int index = 0;
    private int currentSize = 0;
    private FillBufferHandler<I, O> transformer;

    public FillBufferIterator(AbstractIterator<I> inIter, int batchSize, FillBufferHandler<I, O> transformer) {
        this.iter = inIter;
        this.transformer = transformer;
        buffer = new GenericKeyValue[batchSize];
        for (int i = 0; i < batchSize; i++) {
            buffer[i] = new GenericKeyValue(null, null);
        }
        this.batchSize = batchSize;
    }

    public boolean hasNext() {
        if (state != FillState.Complete) {
            while (index < currentSize) {
                if (buffer[index].getValue() != null) {
                    return true;
                }
                index++;
            }
            try {
                return fillAndTransform();
            } catch (Exception e) {
                Log.util.error("Unable to fill buffer %s %e", e, e);
                return false;
            }
        }
        return false;
    }

    public O next() {
        return buffer[index++].getValue();
    }

    public void remove() {

    }

    private boolean fillAndTransform() throws Exception {
        if (grabMoreKeys()) {
            transformer.fill(buffer, currentSize);
            return true;
        }

        return false;
    }

    private boolean grabMoreKeys() throws Exception {
        if (state == FillState.Complete) {
            return false;
        }
        currentSize = 0;
        index = 0;
        for (int i = 0; i < batchSize; i++) {
            if (iter.hasNext()) {
                I curr = iter.next();
                if (curr != null) {
                    buffer[currentSize].setKey(curr);
                    currentSize++;
                }
            } else {
                state = FillState.LastRound;
                iter.close();
                break;
            }
        }
        if (currentSize == 0) {
            state = FillState.Complete;
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        iter.close();
    }


    enum FillState {
        Init, LastRound, Complete
    }
}
