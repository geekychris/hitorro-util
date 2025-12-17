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
package com.hitorro.util.commandandcontrol.ano;

import com.hitorro.util.core.KeyValue;

/**
 * Defines withing a ResponseDefinition which columns participate in a group.  A group defines a subset of columns that
 * act as a tuple. when rendered to xml this means that they get grouped together in a sub element and can support
 * repeated values as a tuple set. This is a cheap way to implement more complex shapes than a simple column row model
 * of output.
 */
public @interface ColumnGroup {
    /**
     * Column index (starting at 0) that indicates the starting column
     *
     * @return
     */
    int start();

    /**
     * Number of columns participating in this group.
     *
     * @return
     */
    int size();

    /**
     * Name of the group that is used in rendering to xml
     *
     * @return
     */
    String name();

    /**
     * Class that is used for rendering the group.
     *
     * @return
     */
    Class groupType() default KeyValue.class;
}
