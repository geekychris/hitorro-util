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
package com.hitorro.util.commandandcontrol.xml;

import java.util.HashMap;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
class XMLJUnitResponseMsg {
    private String m_message;
    private XMLJUnitResponseMsg.Type m_type;

    public XMLJUnitResponseMsg(XMLJUnitResponseMsg.Type type, String message) {
        m_type = type;
        m_message = message;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(String message) {
        m_message = message;
    }

    public XMLJUnitResponseMsg.Type getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = XMLJUnitResponseMsg.Type.getTypeName(type);
    }


    public enum Type {
        info("info"), warning("warning"), error("error");

        private static HashMap<String, XMLJUnitResponseMsg.Type> s_byShortName;
        private String m_type;


        Type(String type) {
            m_type = type;
            setMapEntry(this);
        }

        static XMLJUnitResponseMsg.Type getTypeName(String name) {
            return s_byShortName.get(name.toLowerCase());
        }

        private static void setMapEntry(XMLJUnitResponseMsg.Type type) {
            if (s_byShortName == null) {
                s_byShortName = new HashMap<String, XMLJUnitResponseMsg.Type>();
            }
            s_byShortName.put(type.getName(), type);
        }

        String getName() {
            return m_type;
        }
    }

}
