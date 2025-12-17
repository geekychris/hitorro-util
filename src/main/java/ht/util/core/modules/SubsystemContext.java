package ht.util.core.modules;


import ht.util.core.HTAssert;
import ht.util.core.Log;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Copyright (c) 2003-2008 HiTorro, Inc.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 9:54:38 AM
 * <p/>
 * Description:
 */
public class SubsystemContext {
    private static Map s_subsystemsByName = new Hashtable();
    private static List s_subsystems = new Vector();

    private static String s_systemName;
    private static boolean s_primed = false;

    /**
     * Initialize the system
     *
     * @param name The name of the system, usually the main server name
     */
    public static void initSystem(String name) {
        s_systemName = name;
        s_primed = true;
    }

    public static String getSystemName() {
        return s_systemName;
    }

    public static boolean addSubsystem(String className) {
        HTAssert.assertThat(s_primed, "SubsystemContext has not been given a name, cannot initialze stack");
        try {
            Class cls = Class.forName(className);
            if (cls != null) {
                Object system = cls.newInstance();
                if (system instanceof SubsystemModule) {
                    SubsystemModule sm = (SubsystemModule) system;

                    if (sm.init() == true) {
                        Log.util.info("Initialized subsystem module: %s", sm.getName());
                    } else {
                        Log.util.warn("Could not initialize subsystem module: %s", sm.getName());

                    }
                    s_subsystems.add(sm);
                    s_subsystemsByName.put(sm.getName(), sm);

                }
            }
        } catch (ClassNotFoundException cnfe) {
            Log.util.warn("Unable to find class %s, got error %s", className, cnfe);
            return false;
        } catch (IllegalAccessException iae) {
            Log.util.warn("Unable to find class %s, got error %s", className, iae);
            return false;
        } catch (InstantiationException ie) {
            Log.util.warn("Unable to find class %s, got error %s", className, ie);
            return false;

        }

        return true;
    }

    /*
        It will be really expected that if you are using the module as a compContext then
        one should put a static accessor on the module to get it rather than using
        this expensive hashtable lookup.
    */
    public static SubsystemModule getModule(String moduleName) {
        return (SubsystemModule) s_subsystemsByName.get(moduleName);
    }

    public static boolean deInitSubsystems() {

        int size = s_subsystems.size() - 1;
        for (int i = size; i >= 0; i--) {
            SubsystemModule sm = (SubsystemModule) s_subsystems.get(i);
            Log.util.info("Deinitializing module: %s", sm.getName());
            sm.deinit();
        }
        return true;
    }

}
