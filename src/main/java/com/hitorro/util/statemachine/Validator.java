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
import com.hitorro.util.core.Constants;
import com.hitorro.util.statemachine.scoreboard.StateRow;

/**
 * User: chris
 * Date: Feb 9, 2007
 * Time: 8:36:06 AM
 */

/**
 * Description: Validate that we can transition from the current state to the desired state
 */
public abstract class Validator<E> {
    private JVS m_parameters = null;

    public void setParameters(JVS parameters) {
        m_parameters = parameters;
    }

    public abstract boolean validate(E elem, DirectedEdge edge, State nextState);

    protected boolean logTrue(String reason, E elem, State currentState, State nextState) {
        Log.statemachine.debug(
                "Validator %s returned true for %s testing transition %s to %s, reason %s",
                getName(), elem, currentState, nextState, reason);
        return true;
    }

    protected boolean logFalse(String reason, E elem, State currentState, State nextState) {
        Log.statemachine.debug(
                "Validator %s returned false for %s testing transition %s to %s, reason %s",
                getName(), elem, currentState, nextState, reason);
        return false;
    }

    protected String getName() {
        return getClass().getName();
    }

    /**
     * Determine if the current row has "matured", that it has not been touched in at least n seconds.
     *
     * @param elem
     * @return true if matured.
     */
    protected boolean lastTouchedOlderThan(StateRow elem, int secs) {
        long olderThanTime = System.currentTimeMillis() - secs * Constants.MillisInSecond;


        return elem.getLastTouchedTime() < olderThanTime;
    }
}