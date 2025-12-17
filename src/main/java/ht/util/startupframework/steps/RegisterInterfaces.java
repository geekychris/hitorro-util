package ht.util.startupframework.steps;

import ht.util.core.error.ErrorCode;
import ht.util.startupframework.ServiceContext;


/**
 * Call the register mechanism of a service to register any hooks.
 */
public class RegisterInterfaces implements ServiceStep {
    public static final String EventName = "RegisterInterfaces";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "RegisterInterfaces";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        for (Class intf : ServiceContext.getSC().getNeededInterfaces()) {
            if (ServiceContext.getSC().getServiceInterface(intf) == null) {
                // we dont have a required interface
                return new ErrorCode(30, "Interface %s was required but not defined by any service", new Object[]{intf});
            }
        }
        return null;
    }
}
