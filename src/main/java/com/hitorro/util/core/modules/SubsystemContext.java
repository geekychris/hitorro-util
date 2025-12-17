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
package com.hitorro.util.core.modules;


import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.Log;

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
