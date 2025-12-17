package ht.util.startupframework.steps;

import ht.util.core.Log;
import ht.util.core.error.ErrorCode;
import ht.util.core.string.StringUtil;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.ServiceWrapper;


/**
 * Call each service that has a start method to get that service to finally start up.  Start comes after all services
 * have been initialized. Call this a two phase init.  All services are initialized and then all services are started.
 */
public class Start implements ServiceStep {
    public static final String EventName = "Start";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "Start";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        ServiceContext.getSC().setState(ServiceContext.State.Initializing);
        for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
            Log.servicecontext.info("Starting %s module", module.getShortName());
            String text = module.start(initDb);

            if (!StringUtil.nullOrEmptyString(text)) {
                return new ErrorCode(50, "Unable to start module %s with error %s",
                        new Object[]{module.getShortName(), text});

            }
        }
        return null;
    }
}
