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
package com.hitorro.util.statemachine.csvconsumers;

import com.hitorro.util.statemachine.MooreStateMachine;

/**
 */
public class StateConsumer extends BaseConsumer {
    public static final String StateName = "StateName";
    public static final String Retries = "Retries";
    public static final String Group = "Group";
    public static final String Action = "Action";
    public static final String Description = "Description";
    public static final String RecoveryState = "RecoveryState";
    public static final String ActionParameters = "ActionParameters";

    public StateConsumer(MooreStateMachine registry) {
        super(registry);
    }

    protected void processRow(String[] line) {
        m_registry.addState(getField(StateName, line),
                getField(Group, line),
                getField(Description, line),
                getField(Action, line),
                getField(Retries, line),
                getField(RecoveryState, line),
                getField(ActionParameters, line));
    }
}