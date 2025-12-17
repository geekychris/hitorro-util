package ht.util.integrationevents;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.ListUtil;
import ht.util.core.classes.ClassUtil;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringListFromDelimitedKey;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.PropaccessError;

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
    private List<String> m_eventNames = null;
    private List<String> m_initDBEvents = new ArrayList<String>();

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
        m_eventNames = InitDBIntegrationEvents.apply();
    }

    public List<String> getEventNames() {
        return m_eventNames;
    }

    public boolean hasEvent(String event) {
        return m_eventNames.contains(event);
    }

    /**
     * put an event that is specific to a type of server.
     *
     * @param eventName
     */
    public void addInitDBEvent(String eventName) {
        if (!m_initDBEvents.contains(eventName)) {
            // put to the listFiles of events to run at initdb
            m_initDBEvents.add(eventName);
            // put to the complete listFiles of events
            m_eventNames.add(eventName);
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

        if (!ListUtil.nullOrEmpty(m_initDBEvents)) {
            for (String event : m_initDBEvents) {
                if (runEvent(event)) {
                    Log.integration.info("Ran integration event %s successfully", event);
                } else {
                    Log.integration.info("Failed to run integration event %s", event);
                }
            }
        }
    }


    public boolean runEvent(String eventName) throws PropaccessError {
        if (m_eventNames == null) {
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
