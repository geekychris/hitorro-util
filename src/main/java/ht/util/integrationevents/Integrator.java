package ht.util.integrationevents;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Integration interface implemented by those who wish to be invoked to load some events.
 *
 * @author chris
 */
public interface Integrator {
    /**
     * Run the integration event and return true if successfull.
     *
     * @param args
     * @param name
     * @return
     */
    boolean integrate(JsonNode args, String name);
}
