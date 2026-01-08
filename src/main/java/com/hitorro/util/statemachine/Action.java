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
package com.hitorro.util.statemachine;

import com.hitorro.jsontypesystem.JVS;

/**
 */
public abstract class Action<E, C> {
    private JVS m_parameters = null;

    public void setParameters(JVS parameters) {
        m_parameters = parameters;
    }

    /**
     * @return true means launch modifier in own thread.
     */
    public abstract boolean runInSeperateThread();

    /**
     * @param elem
     * @param containingStructure, this could be a scoreboard or nothing at all if the elements dont need external
     *                             compContext
     * @return
     * @throws Exception
     */
    public abstract boolean modifyState(E elem, C containingStructure) throws Exception;

    /**
     * Some states when we recover need to go to a previous state (merger with filer for instance)
     *
     * @return
     */
    public State recoveryState() {
        return null;
    }

}
