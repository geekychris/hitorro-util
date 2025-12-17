package ht.util.core.thread;

public abstract class ThreadStash<E> {
    public ThreadLocal<E> threadLocal = new ThreadLocal<E>();

    public E get() {
        E res = threadLocal.get();
        if (res == null) {
            res = getNew();
            threadLocal.set(res);
        }
        return res;
    }

    public abstract E getNew();
}