package ht.util.core.queue;

/**
 * Bounded q used by threaded queue implementation.
 *
 * @param <E>
 * @author chris
 */
public class BoundedQueue<E> implements Queue<E> {

    protected int back = -1;

    protected int count = 0;

    protected int front = 0;

    protected E rep[];

    protected int size = 0;

    private EmptyQueueCallback m_callback;

    @SuppressWarnings("unchecked")
    public BoundedQueue(int size) {
        if (size > 0) {
            this.size = size;
            rep = (E[]) new Object[size];
            back = size - 1;
        }
    }

    public void setQueueEmptyCallback(EmptyQueueCallback q) {
        m_callback = q;
    }

    public boolean remove(final Object o) {
        return false;
    }

    public E dequeue() {
        E result = null;
        if (!isEmpty()) {
            result = rep[front];
            rep[front] = null;
            front++;
            if (front >= size) {
                front = 0;
            }
            count--;
        } else {
            if (m_callback != null) {
                // optionally call the empty handler to fill er up
                // or just to report empty q
                m_callback.empty();
            }
        }
        return result;
    }

    public E peek() {
        if (!isEmpty()) {
            return rep[front];
        }
        return null;
    }

    public void enqueue(E e) {
        if (e != null && !isFull()) {
            back++;
            if (back >= size) {
                back = 0;
            }
            rep[back] = e;
            count++;
        }
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return (count == 0);
    }

    public boolean isFull() {
        return (count == size);
    }

    public void clear() {
        count = 0;
        front = 0;
        back = size - 1;
    }

    @Override
    public int capacity() {
        return size;
    }
}
