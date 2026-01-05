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
package com.hitorro.util.startupframework.steps;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.error.ErrorCode;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.ServiceWrapper;
import com.hitorro.util.startupframework.ServicesVersion;

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
                    FileUtil.ensureDirectoryExists(versionsDir);
                    if (module == null) {
                        System.out.println();
                    }
                    if (versionsDir == null) {
                        System.out.println();
                    }
                    try {
                        serviceFile = new File(versionsDir, module.getShortName());
                    }catch (Exception e) {
                        System.out.println();
                    }
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
