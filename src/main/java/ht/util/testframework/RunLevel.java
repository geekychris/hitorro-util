package ht.util.testframework;

import java.util.HashMap;

/**
 *
 */
public enum RunLevel {
    None("none"), Smoke("smoke"), Full("full"), All("all"), Stress("stress"), Never("never");


    private static HashMap<String, RunLevel> s_byShortName;
    private String m_name;


    RunLevel(String name) {
        m_name = name.toLowerCase();
        setMapEntry(this);
    }

    public static RunLevel getFilterByName(String name) {
        return s_byShortName.get(name.toLowerCase());
    }

    public static int size() {
        return s_byShortName.size();
    }

    private static void setMapEntry(RunLevel filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, RunLevel>();
        }
        s_byShortName.put(filter.getName(), filter);
    }

    public String getName() {
        return m_name;
    }
}