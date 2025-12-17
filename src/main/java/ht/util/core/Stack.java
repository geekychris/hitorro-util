package ht.util.core;

/**
 *
 */
public interface Stack<T extends Object> {
    /**
     * Insert a new item into the stack.
     *
     * @param x the item to insert.
     */
    void push(T x);

    /**
     * Remove the most recently inserted item from the stack.
     *
     * @throws UnderflowException if the stack is empty.
     */
    void pop();

    /**
     * Get the most recently inserted item in the stack. Does not alter the stack.
     *
     * @return the most recently inserted item in the stack.
     * @throws UnderflowException if the stack is empty.
     */
    T top();


    /**
     * Return and remove the most recently inserted item from the stack.
     *
     * @return the most recently inserted item in the stack.
     * @throws UnderflowException if the stack is empty.
     */
    T topAndPop();

    /**
     * Test if the stack is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Make the stack logically empty.
     */
    void makeEmpty();
}
