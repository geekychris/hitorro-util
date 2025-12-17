package ht.util.commandandcontrol;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.ano.ArgType;
import ht.util.json.keys.BaseMappingProperty;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */
public class DebugCommandArg implements Comparable<DebugCommandArg> {
    private boolean m_required;

    private BaseMappingProperty m_propKey;

    private ArgType argType = ArgType.Regular;

    public DebugCommandArg(boolean required, BaseMappingProperty key, ArgType argType) {
        m_propKey = key;
        m_required = required;
        this.argType = argType;
    }

    public DebugCommandArg(boolean required, BaseMappingProperty key) {
        this(required, key, ArgType.Regular);
    }

    public ArgType getArgType() {
        return argType;
    }

    public boolean isHidden() {
        return argType != ArgType.Regular;
    }

    public Object getPropValue(JVS jvs) {
        if (m_propKey != null) {
            return m_propKey.apply(jvs);
        }

        return null;
    }

    public BaseMappingProperty getJsonPropertyKey() {
        return m_propKey;
    }

    public boolean getRequired() {
        return m_required;
    }

    public String getName() {
        return this.m_propKey.toString();
    }

    public String validate(JVS map) {
        m_propKey.validate(map);
        return null;
    }

    public String getDescription() {
        return this.m_propKey.getDescription();
    }

    public int compareTo(final DebugCommandArg o) {
        return getName().compareTo(o.getName());
    }
}
