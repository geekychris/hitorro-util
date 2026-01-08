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
package com.hitorro.util.statemachine.scoreboard.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.statemachine.State;
import com.hitorro.util.statemachine.scoreboard.StateRow;

/**
 * <p/>
 * Match rows that have a given state.
 */
public class HasStateConstraint implements HTPredicate<StateRow> {
    private State m_state;

    public HasStateConstraint(State state) {
        m_state = state;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "HasStateConstraint.initFromMap not implemented");
        return false;
    }

    public boolean test(StateRow stateRow) {
        return stateRow.getState().equals(m_state);
    }

    public void initForPass() {

    }
}
