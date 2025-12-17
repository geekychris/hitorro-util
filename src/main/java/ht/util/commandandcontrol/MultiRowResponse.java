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
package ht.util.commandandcontrol;

/**
 * wrapper that allows one to construct a row that contains repeating fields (jagged data).
 *
 * @author chris
 */
public abstract class MultiRowResponse {
    protected ResponseShape shape;
    protected RenderingContainer containers[];

    @SuppressWarnings("unchecked")
    public MultiRowResponse(int columns, ResponseShape r) {
        shape = r;
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
    }

    public void setRenderingRow(RenderingContainer... containers) {
        this.containers = containers;
    }

    public abstract void addTupleArray(int offset, Object elems[]);

    public abstract void addThrowable(int column, Throwable t, int stackDepth, int startFrom);

    public abstract void clear();

    public abstract boolean add(int index, Object addMe);

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public abstract void addToResponse(Response response);
}
