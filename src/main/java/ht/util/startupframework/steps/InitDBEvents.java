package ht.util.startupframework.steps;

import ht.util.core.error.ErrorCode;
import ht.util.integrationevents.IntegrationEventsContext;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.startupframework.ServiceContext;


/**
 * Call an registered database load events if in the initdb mode.  These are defined in the configs and read by the
 * integration events subsystem.  Simply this is in the service framework to allow for reloading of data following the
 * reinitialization of the server.
 */
public class InitDBEvents implements ServiceStep {
    public static final String EventName = "InitDBEvents";

    @Override
    public String getPhaseName() {
        return "InitDBEvents";
    }

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        if (initDb) {
            ServiceContext.getSC().setState(ServiceContext.State.LoadingEvents);
            try {
                IntegrationEventsContext.getContext().runInitDBEvents();
            } catch (PropaccessError propaccessError) {
                return null;
            }
        }
        return null;
    }
}
