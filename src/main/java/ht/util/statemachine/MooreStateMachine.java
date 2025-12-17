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
package ht.util.statemachine;

import ht.util.core.classes.ClassUtil;
import ht.util.core.map.PassThroughHashMap;
import ht.util.core.params.HTProperties;
import ht.util.core.string.StringUtil;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:34:35 AM
 * <p/>
 * Machine holds all the finite states, groups.  States hold what the adjacent edges are along with the action that must
 * be performed when entering the state.
 */
@TypeClassMetaInfo(shortTypeName = "MooreStateMachine",
        isView = false,
        isPersisted = false,
        schemaVersion = MooreStateMachine.SerializationVersion)
public class MooreStateMachine implements HTSerializable {
    public static final int SerializationVersion = 2;
    private Map<String, Group> m_groups = new HashMap<String, Group>();
    private Map<String, State> m_states = new HashMap<String, State>();
    private Map<String, String> m_properties = new PassThroughHashMap<String, String>(HTProperties.getProperties().getMap());


    public State[] getStates() {
        Collection<State> states = m_states.values();
        State[] s = new State[m_states.size()];
        int i = 0;
        for (Iterator<State> iterator = states.iterator(); iterator.hasNext(); ) {
            s[i++] = iterator.next();
        }
        return s;
    }


    /**
     * Get the group from its group name.
     *
     * @param name
     * @return Group object
     */
    public Group getGroup(String name) {
        return m_groups.get(name);
    }

    /**
     * Get the state object from its name.
     *
     * @param state
     * @return
     */
    public State getState(String state) {
        return m_states.get(state);
    }

    public void finalizeGroups() {
        Collection<Group> groups = m_groups.values();
        for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext(); ) {
            Group group = iterator.next();
            group.finalizeInit(this);
        }
    }

    public void addGroup(String name, String parent, String description) {
        Group group = new Group(name, parent, description);
        m_groups.put(name, group);
    }

    /**
     * Assumes that the group information is already initialized.
     *
     * @param name
     * @param group
     * @param description
     * @param stateModifierClass
     */
    public boolean addState(String name,
                            String group,
                            String description,
                            String stateModifierClass,
                            String retries,
                            String recoveryState,
                            String stateModifierParameters) {
        Action modifier = null;
        if (!StringUtil.nullOrEmptyOrBlankString(stateModifierClass)) {
            modifier = getStateModifier(stateModifierClass);
            if (modifier == null) {
                Log.statemachine.error("State Modifier for state %s could not be created.", name, stateModifierClass);
                return false;
            } else {
                Log.statemachine.debug("State modifier for state %s initialized, %s", name, stateModifierClass);
            }
        }

        State state = getNewState();
        state.setName(name);
        state.setDescription(description);
        state.setStateCode(0);
        state.setGroup(getGroup(group));
        state.setErrorState(false);
        state.setModifier(modifier);
        state.setStateModifierRetries(Integer.parseInt(retries));
        state.setRecoveryState(getState(recoveryState));
        state.setActionParameters(stateModifierParameters);
        m_states.put(name, state);
        return true;
    }

    protected State getNewState() {
        return new State();
    }

    /**
     * @param current
     * @param next
     * @param description
     * @param stateValidatorClass
     * @return
     */
    public boolean addStateTransition(String current,
                                      String next,
                                      String description,
                                      String stateValidatorClass,
                                      String uiChoice, String edgeParameters) {
        Validator validator = null;
        if (!StringUtil.nullOrEmptyOrBlankString(stateValidatorClass)) {
            validator = getStateValidator(stateValidatorClass);
            if (validator == null) {
                Log.statemachine.error("DirectedEdge %s to %s, validator could not be created %s", current, next, stateValidatorClass);
                return false;
            } else {
                Log.statemachine.debug("State validator for state %s to %s initialized, %s", current, next, stateValidatorClass);
            }
        }

        State currentState = getState(current);
        State nextState = getState(next);
        if (currentState == null) {
            Log.statemachine.error("DirectedEdge %s to %s, current state invalid", current, next);
            return false;
        }
        if (nextState == null) {
            Log.statemachine.error("DirectedEdge %s to %s, next state invalid", current, next);
            return false;
        }

        DirectedEdge stateT = new DirectedEdge(currentState, nextState, description, validator, uiChoice, edgeParameters);
        currentState.addStateTransition(stateT);
        return true;
    }

    private Action getStateModifier(String className) {
        Object o = ClassUtil.getInstanceSwallowError(className);
        if (o == null) {
            return null;
        }
        if (o instanceof Action) {
            return (Action) o;
        }
        return null;
    }

    private Validator getStateValidator(String className) {
        Object o = ClassUtil.getInstanceSwallowError(className);
        if (o == null) {
            return null;
        }
        if (o instanceof Validator) {
            return (Validator) o;
        }
        return null;
    }


    public Map<String, String> getProperties() {
        return m_properties;
    }


    public String getStringProperty(String key, String defaultValue) {
        key = key.toLowerCase();

        return m_properties.get(key);
    }


    public void setProperty(String key, String value) {
        m_properties.put(key.toLowerCase(), value);
    }


    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        //   version 2
        writeStringToParameters(os, this.m_properties);

        //   version 1
        os.writeInt(getSerializationVersion());
        writeStringToGroup(os, this.m_groups);
        writeStringToState(os, this.m_states);
    }

    public void writeStringToGroup(HTObjectOutputStream os, Map<String, Group> map) throws IOException, StoreException {
        if (map == null) {
            os.writeInt(0);
            return;
        }
        os.writeInt(map.size());
        Set<Map.Entry<String, Group>> set = map.entrySet();
        for (Map.Entry<String, Group> entry : set) {
            os.writeString(entry.getKey());
            os.writeVersionedObject(entry.getValue());
        }
    }

    public void writeStringToState(HTObjectOutputStream os, Map<String, State> map) throws IOException, StoreException {
        if (map == null) {
            os.writeInt(0);
            return;
        }
        os.writeInt(map.size());
        Set<Map.Entry<String, State>> set = map.entrySet();
        for (Map.Entry<String, State> entry : set) {
            os.writeString(entry.getKey());
            os.writeVersionedObject(entry.getValue());
        }
    }


    public void writeStringToParameters(HTObjectOutputStream os, Map<String, String> map) throws IOException {
        if (map == null) {
            os.writeInt(0);
            return;
        }

        os.writeInt(map.size());
        Set<Entry<String, String>> set = map.entrySet();
        for (Map.Entry<String, String> entry : set) {
            os.writeString(entry.getKey());
            os.writeString(entry.getValue());
        }
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 2:
                m_properties = readStringToParameters(os);

            case 1:
                m_groups = readStringToGroup(os);
                m_states = readStringToState(os);
        }
    }

    public Map<String, Group> readStringToGroup(HTObjectInputStream os)
            throws IOException, StoreException, ClassNotFoundException {
        Map<String, Group> map = new HashMap<String, Group>();
        int size = os.readInt();
        for (int i = 0; i < size; i++) {
            String key = os.readString();
            Group pts = (Group) os.readVersionedObject();
            map.put(key, pts);
        }
        return map;
    }

    public Map<String, State> readStringToState(HTObjectInputStream os)
            throws IOException, StoreException, ClassNotFoundException {
        Map<String, State> map = new HashMap<String, State>();
        int size = os.readInt();
        for (int i = 0; i < size; i++) {
            String key = os.readString();
            State pts = (State) os.readVersionedObject();
            map.put(key, pts);
        }
        return map;
    }

    public TreeMap<String, String> readStringToParameters(HTObjectInputStream is) throws IOException, ClassNotFoundException {
        TreeMap<String, String> map = new TreeMap<String, String>();

        int size = is.readInt();

        for (int i = 0; i < size; i++) {
            String key = is.readString();
            String value = is.readString();
            map.put(key, value);
        }

        return map;
    }

    public int getSerializationVersion() {
        return SerializationVersion;
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
}





