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
package ht.util.statemachine.scoreboard.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.statemachine.Group;
import ht.util.statemachine.State;
import ht.util.statemachine.scoreboard.StateRow;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:52:53 PM
 * <p/>
 * Match rows that have a state that belong to a state group.  If hierarchy test is set then if we have a constraint of
 * a, where a contains b which contains c, then if we present an item of group c, this is a test.
 */
public class HasGroupConstraint implements HTPredicate<StateRow> {
    private Group m_group;
    private boolean m_hierarchyMatch;

    public HasGroupConstraint(Group group, boolean hiearchyMatch) {
        m_group = group;
        m_hierarchyMatch = hiearchyMatch;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "HasGroupConstraint.initFromMap not implemented");
        return false;
    }

    public boolean test(StateRow stateRow) {
        State s = stateRow.getState();
        if (s == null) {
            return false;
        }

        Group g = s.getGroup();
        if (g == null) {
            return false;
        }

        if (m_hierarchyMatch) {
            Group t = g;
            while (t != null) {
                if (m_group.equals(t)) {
                    return true;
                }
                t = t.getParent();
            }
            return false;
        } else {
            return m_group.equals(g);
        }
    }

    public void initForPass() {

    }
}
