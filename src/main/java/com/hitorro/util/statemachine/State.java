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
import com.hitorro.util.core.CommandArgs;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.text.ParseException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 8, 2007 Time: 6:02:41 PM
 * <p/>
 * Defines a state of a finite state machine to be incorporated into a simple workflow or score board.
 */

@TypeClassMetaInfo(shortTypeName = "State",
        isView = false,
        isPersisted = false,
        schemaVersion = State.SerializationVersion)
public class State<E> implements HTSerializable {
    public static final int SerializationVersion = 1;

    private String name;
    private String description;
    private int stateCode;
    private boolean errorState;
    private Group group;
    private Action m_modifier;
    private String m_parameters;
    private int stateModifierRetries = 0;
    private State recoveryState;
    private DirectedEdge[] edges = new DirectedEdge[0];
    private E stateInfo;

    public State() {

    }

    public State(String name,
                 Group group,
                 String description,
                 int stateOrdinal,
                 Action modifier,
                 int retries,
                 State recoveryState,
                 String actionParameters) {
        this(name, description, stateOrdinal, group, false, modifier, retries, recoveryState, actionParameters);
    }


    public State(String name,
                 String description,
                 int stateOrdinal,
                 Group group,
                 boolean errorState,
                 Action modifier,
                 int retriesAllowed,
                 State recoveryState,
                 String actionParameters) {
        setName(name);
        setDescription(description);
        setStateCode(stateOrdinal);
        setGroup(group);
        setErrorState(errorState);
        setModifier(modifier);
        setStateModifierRetries(retriesAllowed);
        setRecoveryState(recoveryState);
        setActionParameters(actionParameters);
        setActionParameters();
    }

    public void setMe(Object[] o) {

    }

    private void setActionParameters() {
        if (m_modifier != null) {
            m_modifier.setParameters(getParameters());
        }
    }

    public JVS getParameters() {
        try {
            if (StringUtil.nullOrEmptyString(m_parameters)) {
                return null;
            }

            return CommandArgs.getParameters(m_parameters, true, true);
        } catch (ParseException | PropaccessError pe) {
            Log.statemachine.error("Unable to parse parameters <%s> with error %s", m_parameters, pe.getMessage());
            return null;
        }
    }

    public void setActionParameters(String params) {
        m_parameters = params;
        setActionParameters();
    }

    /**
     * If this state is considered unstable on a system recovery, we need another state to place this guy into.
     *
     * @return
     */
    public State getRecoveryState() {
        return recoveryState;
    }

    public void setRecoveryState(State recoveryState) {
        this.recoveryState = recoveryState;
    }

    public int getRetriesAllowed() {
        return stateModifierRetries;
    }

    public String toString() {
        return getName();
    }

    public final String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public final int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    /**
     * Defines if this is a state that is a one that represents an error.
     *
     * @return
     */
    public final boolean isErrorState() {
        return errorState;
    }

    public void setErrorState(boolean errorState) {
        this.errorState = errorState;
    }

    /**
     * Get the group this state belongs to.  Groups allow us to simplify the logic tests
     *
     * @return
     */
    public final Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Get the set of state edges that this state can attempt to take to enter a new state.
     *
     * @return
     */
    public final DirectedEdge[] getStateTransitions() {
        return edges;
    }

    /**
     * This equality test assumes singleton per state.  That is, we dont duplicate state.
     *
     * @param state
     * @return true if an exact state test
     */
    public boolean equalsState(State state) {
        return state == this;
    }

    /**
     * We MUST be comparing the same instance of groups.  This is because we dont replicate group objects in the system
     * (they are flyweights).
     *
     * @param state
     * @return true if an exact group test
     */
    public boolean equalsGroup(State state) {
        return state.getGroup() == this.group;
    }

    /**
     * Verify if one can transition from the current state to the next state.
     *
     * @param newState
     * @return
     */
    public boolean isStateTransitionLegal(State newState) {
        for (int i = 0; i < edges.length; i++) {
            if (edges[i].getNextState() == newState) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simple way of adding to the array of states a new state. Not optimal
     *
     * @param transition
     */
    public void addStateTransition(DirectedEdge transition) {
        DirectedEdge[] newTran = new DirectedEdge[edges.length + 1];
        System.arraycopy(edges, 0, newTran, 0, edges.length);
        newTran[edges.length] = transition;
        edges = newTran;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeString(name);
        os.writeString(description);
        os.writeInt(stateCode);
        os.writeBoolean(errorState);
        os.writeVersionedObject(group);
        if (m_modifier != null) {
            os.writeString(m_modifier.getClass().getName());
        } else {
            os.writeString(null);
        }
        os.writeInt(stateModifierRetries);
        os.writeVersionedObject(recoveryState);
        os.writeArrayOfHTSerializable(edges);
        os.writeString(m_parameters);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:
                name = os.readString();
                description = os.readString();
                stateCode = os.readInt();
                errorState = os.readBoolean();
                group = (Group) os.readVersionedObject();
                String clazz = os.readString();
                if (!StringUtil.nullOrEmptyString(clazz)) {
                    m_modifier = (Action) ClassUtil.getInstanceSwallowError(clazz);
                }
                stateModifierRetries = os.readInt();
                recoveryState = (State) os.readVersionedObject();
                edges = (DirectedEdge[]) os.readArrayOfHTSerializable();
                m_parameters = os.readString();
                setActionParameters();
        }

    }

    public int getSerializationVersion() {
        return State.SerializationVersion;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }

    public Action getModifier() {
        return m_modifier;
    }

    public void setModifier(Action m_modifier) {
        this.m_modifier = m_modifier;
    }

    public int getStateModifierRetries() {
        return stateModifierRetries;
    }

    public void setStateModifierRetries(int stateModifierRetries) {
        this.stateModifierRetries = stateModifierRetries;
    }

    public DirectedEdge[] getEdges() {
        return edges;
    }

    /*public void setEdges (DirectedEdge[] edges)
    {
        this.edges = edges;
    } */

    public E getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(E stateInfo) {
        this.stateInfo = stateInfo;
    }

}
