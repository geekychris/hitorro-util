/*

    
    User: chris
*/

package com.hitorro.util.job;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import org.apache.log4j.Level;

/**
 * Simple job that will print a message.
 */
public class MessageJob extends Job {

    public static final String MessageJob = "messagejob";

    public JobExecutionResult doAction(JobParameters parameters) {
        if (!(parameters instanceof MessageJobParameters)) {
            return new JobExecutionResult(Level.ERROR, "MessageJobParameters were not provided, got %s",
                    parameters.getClass().getCanonicalName());
            // we only work off parameters
        }

        MessageJobParameters mjb = (MessageJobParameters) parameters;
        String msg = mjb.getMessage();
        int outkind = mjb.getOutputKind();
        switch (outkind) {
            case MessageJobParameters.ToConsoleMessage:
                Console.println(msg);
                break;
            case MessageJobParameters.ToLogMessage:
                Log.scheduledJobs.info(msg);
                break;
            default:
                break;
        }
        return JobExecutionResult.Executed;
    }

    public String getName() {
        return MessageJob;
    }

    public boolean needsSession() {
        return false;
    }
}
