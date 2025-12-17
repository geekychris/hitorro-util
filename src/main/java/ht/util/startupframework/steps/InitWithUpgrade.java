package ht.util.startupframework.steps;

import ht.util.core.Console;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.error.ErrorCode;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.ServiceWrapper;
import ht.util.startupframework.ServicesVersion;

import java.io.File;
import java.io.IOException;


/**
 * Call the init method of each service.  We keep track of a global service version that can be incremented on code
 * releases.  Each service gets to have its currently initialized state persisted.  The service is called with its
 * current version and the system version to upgrade to.  The idea is that services can fail but we cant or dont want
 * prior successful upgraded services to croak on a re-update.  Therefor if the server starts back up after a failed
 * upgrade only services that failed to upgrade will identify themselfs as not completed.  Once the service succeeds
 * then the new version is stamped on disk.  This is a ratchet type elevator upgrade.
 */
public class InitWithUpgrade implements ServiceStep {
    public static final String EventName = "InitWithUpgrade";

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "InitWithUpgrade";
    }

    @Override
    public ErrorCode execute(boolean initDb) throws IOException {
        File home = Env.getHome();
        File versionsDir = new File(home, "softwareversion");
        FileUtil.ensureDirectoryExists(versionsDir);
        File diskVersionFile = new File(versionsDir, "successversion");

        long diskVersion = FileUtil.readLongStringValFromFileDefaulting(diskVersionFile, 0);
        long softVersion = ServicesVersion.getVersion();
        boolean upgrade = diskVersion < softVersion;
        for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
            if (!module.isInitialized()) {
                Log.servicecontext.info("Initializing %s module", module.getShortName());
                long moduleVersion = diskVersion;
                boolean upgradeService = false;
                File serviceFile = null;
                if (upgrade) {
                    serviceFile = new File(versionsDir, module.getShortName());
                    moduleVersion = FileUtil.readLongStringValFromFileDefaulting(serviceFile, moduleVersion);
                    if (moduleVersion != softVersion) {
                        upgradeService = true;
                    }
                }
                String text = module.init(initDb, upgradeService, moduleVersion, softVersion);
                if (!StringUtil.nullOrEmptyString(text)) {
                    return new ErrorCode(20, "Unable to initialize module %s with error %s",
                            new Object[]{module.getShortName(), text});
                }
                if (upgradeService) {
                    FileUtil.writeLongStringValFromFile(serviceFile, softVersion);
                }
                module.addAllCommands();
            } else {
                Console.println("Module already initialized %s", module);
            }
        }
        if (upgrade) {
            // we can now write out the current version as it upgraded ok (well we got here!)
            FileUtil.writeLongStringValFromFile(diskVersionFile, softVersion);
        }
        return null;
    }
}
