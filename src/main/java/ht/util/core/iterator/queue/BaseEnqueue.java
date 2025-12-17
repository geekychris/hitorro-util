package ht.util.core.iterator.queue;

public abstract class BaseEnqueue<E> extends AbstractEnqueue<E> {

    protected AbstractEnqueue<E> queue;

    public int size() {
        return queue.size();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public Object getNotifier() {
        return queue.getNotifier();
    }

    @Override
    public void setNotifier(final Object notifier) {
        queue.setNotifier(notifier);
    }

    @Override
    public boolean getQueueCanceled() {
        return queue.getQueueCanceled();
    }

    @Override
    public void setQueueCanceled(final boolean flag) {
        queue.setQueueCanceled(flag);
    }

    @Override
    public void setQueueComplete() {
        queue.setQueueComplete();
    }

    @Override
    public boolean getQueueComplete() {
        return queue.getQueueComplete();
    }

    @Override
    public E remove(final Object o) {
        return queue.remove(o);
    }


}
