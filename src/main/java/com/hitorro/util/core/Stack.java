/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

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
