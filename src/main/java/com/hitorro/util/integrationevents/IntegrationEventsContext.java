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
package com.hitorro.util.integrationevents;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringListFromDelimitedKey;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around integration events to manage the execution.
 *
 * @author chris
 */
public class IntegrationEventsContext {
    private static final String ChildKeyKey = "integration.events";
    private static final StringProperty IntegrationClass =
            new StringProperty("integrator", "Integrator class used for the integration", null);

    public static StringListFromDelimitedKey InitDBIntegrationEvents =
            new StringListFromDelimitedKey("integration.initdblist",
                    "listFiles of integration events to load on initdb",
                    ",",
                    null);
    private static IntegrationEventsContext s_context = null;
    private List<String> eventNames = null;
    private List<String> initDBEvents = new ArrayList<String>();

    public IntegrationEventsContext() {
        init();
    }

    public synchronized static IntegrationEventsContext getContext() {
        if (s_context == null) {
            s_context = new IntegrationEventsContext();
        }
        return s_context;
    }

    public void init() {
        eventNames = InitDBIntegrationEvents.apply();
    }

    public List<String> getEventNames() {
        return eventNames;
    }

    public boolean hasEvent(String event) {
        return eventNames.contains(event);
    }

    /**
     * put an event that is specific to a type of server.
     *
     * @param eventName
     */
    public void addInitDBEvent(String eventName) {
        if (!initDBEvents.contains(eventName)) {
            // put to the listFiles of events to run at initdb
            initDBEvents.add(eventName);
            // put to the complete listFiles of events
            eventNames.add(eventName);
        }
    }

    public void runInitDBEvents() throws PropaccessError {
        List<String> initdbEvents = InitDBIntegrationEvents.apply();
        if (!ListUtil.nullOrEmpty(initdbEvents)) {
            for (String event : initdbEvents) {
                if (runEvent(event)) {
                    Log.integration.info("Ran integration event %s successfully", event);
                } else {
                    Log.integration.info("Failed to run integration event %s", event);
                }
            }
        }

        if (!ListUtil.nullOrEmpty(initDBEvents)) {
            for (String event : initDBEvents) {
                if (runEvent(event)) {
                    Log.integration.info("Ran integration event %s successfully", event);
                } else {
                    Log.integration.info("Failed to run integration event %s", event);
                }
            }
        }
    }


    public boolean runEvent(String eventName) throws PropaccessError {
        if (eventNames == null) {
            Log.integration.error("Unable to retrieve a listFiles of integration events");
            return false;
        }
        JsonNode prop = JVSProperties.getProperties().get(Fmt.S("%s.%s", ChildKeyKey, eventName));
        if (prop != null) {
            String clazz = IntegrationClass.apply(prop);
            if (StringUtil.nullOrEmptyOrBlankString(clazz)) {
                Log.integration.error("Event %s does not have an integrator key associated with it", eventName);
                return false;
            }
            Object o = ClassUtil.getInstanceSwallowError(clazz);
            if (o != null) {
                if (o instanceof Integrator) {
                    return ((Integrator) o).integrate(prop, eventName);
                }
            }
        }
        return false;
    }
}
