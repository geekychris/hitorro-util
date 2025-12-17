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
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:35:54 AM
 * <p/>
 * Description:
 * <p/>
 * Describes what a state can transition into.  Other states could be error states. This certainly can be used for
 * verification that we are transitioning to valid states, but also could be used to drive state transitions.
 * <p/>
 * When defining numerous edges for a given state, the Validators should be implemented in a mutually exclusive state,
 * or ordered to execute in a cascade manor.  This is to prevent ambiguity of which path the state machine will progress
 * in, given the same features.
 */
@TypeClassMetaInfo(shortTypeName = "DirectedEdge",
        isView = false,
        isPersisted = false,
        schemaVersion = DirectedEdge.SerializationVersion)
public class DirectedEdge implements HTSerializable {
    public static final int SerializationVersion = 1;
    private String m_description;
    private Validator m_validator;
    private State m_thisState;
    private State m_nextState;
    private String m_uiChoice;
    private String m_parameters;

    public DirectedEdge(State thisState,
                        State nextState,
                        String description,
                        Validator validator,
                        String uiChoice,
                        String parameters) {
        m_thisState = thisState;
        m_nextState = nextState;
        m_description = description;
        m_validator = validator;
        m_uiChoice = uiChoice;
        m_parameters = parameters;
        setValidatorParameters();
    }

    public DirectedEdge() {

    }

    private void setValidatorParameters() {
        if (m_validator != null) {
            m_validator.setParameters(getParameters());
        }
    }

    public String getUIChoice() {
        return m_uiChoice;
    }

    public State getThisState() {
        return m_thisState;
    }

    public State getNextState() {
        return m_nextState;
    }

    public Validator getValidator() {
        return m_validator;
    }

    public String getDescription() {
        return m_description;
    }

    /*
    public static final int SerializationVersion = 1;
    private String m_description;
    private Validator m_validator;
    private State m_thisState;
    private State m_nextState;

     */

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());

        os.writeString(m_description);
        if (m_validator != null) {
            os.writeString(m_validator.getClass().getCanonicalName());
        } else {
            os.writeString(null);
        }
        os.writeVersionedObject(m_thisState);
        os.writeVersionedObject(m_nextState);
        os.writeString(m_uiChoice);
        os.writeString(m_parameters);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:
                m_description = os.readString();
                String clazz = os.readString();
                if (!StringUtil.nullOrEmptyString(clazz)) {
                    m_validator = (Validator) ClassUtil.getInstanceSwallowError(clazz);
                }
                m_thisState = (State) os.readVersionedObject();
                m_nextState = (State) os.readVersionedObject();
                m_uiChoice = os.readString();
                m_parameters = os.readString();
                setValidatorParameters();
        }

    }

    public int getSerializationVersion() {
        return DirectedEdge.SerializationVersion;
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
}