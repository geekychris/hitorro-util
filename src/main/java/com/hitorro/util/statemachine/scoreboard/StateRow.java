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
package com.hitorro.util.statemachine.scoreboard;

import com.hitorro.util.statemachine.State;

/**
 * <p/>
 * A row that holds
 */
public class StateRow<P> {
    private P m_payload;
    private State m_state;
    private long lastTouchedTime;
    private int m_retries;

    private boolean m_isDeleted = false;

    /**
     * StateRow is not deleted while there potential workers working the scoreboard.  Therefor
     */
    public void delete() {
        m_isDeleted = true;
    }

    public boolean isDeleted() {
        return m_isDeleted;
    }

    public P getPayload() {
        return m_payload;
    }

    public void setPayload(P payload) {
        m_payload = payload;
    }

    public State getState() {
        return m_state;
    }

    public void setState(State s) {
        m_state = s;
    }

    public long getLastTouchedTime() {
        return lastTouchedTime;
    }

    public void setLastTouchedTime(long time) {
        lastTouchedTime = time;
    }

    public void touch() {
        lastTouchedTime = System.currentTimeMillis();
    }

    public int getRetries() {
        return m_retries;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public boolean decrementRetries() {
        if (m_retries > 0) {
            m_retries--;
            return true;
        }
        return false;
    }
}
