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
package com.hitorro.util.commandandcontrol;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.json.keys.BaseMappingProperty;

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
