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
package com.hitorro.util.core.topologicalsort;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Dec 23, 2008 Time: 1:48:16 PM
 * <p/>
 * Description: Interface that caller must implement for all nodes provided to be sorted.  This interface is used to
 * describe its identity and its dependencies so that the sorter can perform its internal book keeping.
 */
public interface NodeInterface<N extends NodeInterface> {
    /**
     * Some identifying key to represent this node
     *
     * @return
     */
    Object getKey();

    /**
     * array of nodes that this node is dependent on.
     *
     * @return
     */
    N[] getDependentNodes();
}
