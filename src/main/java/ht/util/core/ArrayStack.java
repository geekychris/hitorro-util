package ht.util.core;

import java.util.List;

/**
 *
 */
public class ArrayStack<T> implements Stack<T> {

    private static final int DEFAULT_CAPACITY = 10;
    private Object[] theArray;
    private int topOfStack;

    /**
     * Construct the stack.
     */
    public ArrayStack() {
        theArray = new Object[DEFAULT_CAPACITY];
        topOfStack = -1;
    }

    public void fillList(List<T> list, int size, boolean adjustStack) {
        if (size() >= size()) {
            for (int i = 0; i < size; i++) {
                T t = (T) theArray[topOfStack - size + 1 + i];
                list.add(t);
            }
            if (adjustStack) {
                topOfStack = topOfStack - size;
            }
        } else {
            throw new UnderflowException("ArrayStack top");
        }
    }

    public int size() {
        return topOfStack + 1;
    }

    /**
     * Test if the stack is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return topOfStack == -1;
    }

    /**
     * Make the stack logically empty.
     */
    public void makeEmpty() {
        topOfStack = -1;
    }

    /**
     * Get the most recently inserted item in the stack. Does not alter the stack.
     *
     * @return the most recently inserted item in the stack.
     * @throws UnderflowException if the stack is empty.
     */
    public T top() {
        if (isEmpty()) {
            throw new UnderflowException("ArrayStack top");
        }
        return (T) theArray[topOfStack];
    }

    /**
     * Remove the most recently inserted item from the stack.
     *
     * @throws UnderflowException if the stack is empty.
     */
    public void pop() {
        if (isEmpty()) {
            throw new UnderflowException("ArrayStack pop");
        }
        topOfStack--;
    }

    /**
     * Return and remove the most recently inserted item from the stack.
     *
     * @return the most recently inserted item in the stack.
     * @throws Underflow if the stack is empty.
     */
    public T topAndPop() {
        if (isEmpty()) {
            throw new UnderflowException("ArrayStack topAndPop");
        }
        return (T) theArray[topOfStack--];
    }

    /**
     * Insert a new item into the stack.
     *
     * @param x the item to insert.
     */
    public void push(T x) {
        if (topOfStack + 1 == theArray.length) {
            doubleArray();
        }
        theArray[++topOfStack] = x;
    }

    /**
     * Internal method to extend theArray.
     */
    private void doubleArray() {
        Object[] newArray;

        newArray = new Object[theArray.length * 2];
        for (int i = 0; i < theArray.length; i++) {
            newArray[i] = theArray[i];
        }
        theArray = newArray;
    }


}
