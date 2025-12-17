package ht.util.core.thread.farm;

import ht.util.core.DeInitIntf;
import ht.util.core.iterator.mappers.BaseMapper;

/**
 * Command that performs the work on a queue element. Given Type I and returns O I in most cases be the same type as O
 */
public abstract class FarmCommand<InputType, OutputType, ThreadDateType> extends BaseMapper<InputType, OutputType> {
    /**
     * Unit of work to be carried out. This command must allign whith the input queue and output queue.
     *
     * @param inElement
     * @return
     */
    public abstract OutputType apply(InputType inElement);

    public ThreadDateType getThreadData() {
        return (ThreadDateType) ((FarmThread) Thread.currentThread()).getThreadData();
    }

    public void setThreadData(ThreadDateType data) {
        ((FarmThread) Thread.currentThread()).setThreadData(data);
    }

    public ThreadDateType getThreadData(FarmThread ft) {
        return (ThreadDateType) ft.getThreadData();
    }

    public void commit() {
        // subclass expected to make sense of a commit if it assumes that the command was used in some
        // kind of transactional process.
    }

    public void rollback() {
        // subclass expected to make sense of a rollback if it assumes that the command was used in some
        // kind of transactional process.
    }

    /**
     * If the payload implements the DeInit interface then we call it
     *
     * @param ft
     */
    public void deinit(FarmThread ft) {
        ThreadDateType fdt = getThreadData(ft);
        if (fdt != null && fdt instanceof DeInitIntf) {
            ((DeInitIntf) fdt).deinit();
        }
    }

}
