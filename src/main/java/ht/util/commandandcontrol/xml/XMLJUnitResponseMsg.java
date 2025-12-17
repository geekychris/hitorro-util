package ht.util.commandandcontrol.xml;

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
