package ht.util.statemachine.scoreboard;

import ht.util.statemachine.State;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:13:37 PM
 * <p/>
 * A row that holds
 */
public class StateRow<P> {
    private P m_payload;
    private State m_state;
    private long m_lastTouchedTime;
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
        return m_lastTouchedTime;
    }

    public void setLastTouchedTime(long time) {
        m_lastTouchedTime = time;
    }

    public void touch() {
        m_lastTouchedTime = System.currentTimeMillis();
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
