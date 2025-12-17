package ht.util.startupframework.steps;

import ht.util.core.Log;
import ht.util.core.error.ErrorCode;
import ht.util.core.string.StringUtil;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.ServiceWrapper;


public class RegisterHooks implements ServiceStep {
    public static final String EventName = "RegisterHooks";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "RegisterHooks";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
            if (!module.isInitialized()) {
                Log.servicecontext.info("registering hook %s module", module.getShortName());
                String text = module.registerHooks(initDb);
                if (!StringUtil.nullOrEmptyString(text)) {
                    Log.servicecontext.fatal(
                            "Unable to registerHook in module %s with error %s",
                            module.getShortName(), text);

                    return new ErrorCode(10, "Unable to registerHook in module %s with error %s", new Object[]{module.getShortName(), text});
                }
            }
        }
        return null;
    }
}
