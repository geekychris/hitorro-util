package com.hitorro.util.job;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.log.Logger;
import org.apache.log4j.Level;

/**
 * <p/>
 * Created by a job if it has something to say.  Such as:
 * <p/>
 * I failed, please retry me later. I failed and I am giving up Here is my update parameters for the retry.
 */
public class JobExecutionResult {
    public static final JobExecutionResult Executed = new JobExecutionResult();

    private boolean shouldRetry = false;
    private int timeMinutes = 0;
    private JobParameters jobParameters = null;
    private boolean hasMessage = false;
    private Level messageLevel = Level.INFO;
    private String m_message = null;
    private Object messageArgs[] = null;

    private boolean completedRewrite = false;
    private String newEventName;
    private int m_collectionId;

    protected JobExecutionResult() {
        //

    }

    public JobExecutionResult(int retryTimeInMinutes, JobParameters params) {
        shouldRetry = true;
        timeMinutes = retryTimeInMinutes;
        jobParameters = params;
    }

    public JobExecutionResult(Level level, String message, Object... msgArgs) {
        setMessage(level, message, msgArgs);
    }

    /**
     * Set the name and collection Id if we are complete and want to simply rename the PSO (if held in a pso) to a new
     * non queue entry.
     *
     * @param name
     * @param collectionId
     */
    public void setNewEventName(String name, int collectionId) {
        completedRewrite = true;
        newEventName = name;
        m_collectionId = collectionId;
    }

    public boolean getCompleteWithRewrite() {
        return completedRewrite;
    }

    public String getNewEventName() {
        return newEventName;
    }

    public int getCollectionID() {
        return m_collectionId;
    }

    public void setMessage(Level level, String msg, Object... args) {
        messageLevel = level;
        m_message = msg;
        messageArgs = args;
    }

    /**
     * Output the error message to the logger provided.
     *
     * @param logger
     */
    public void applyMessageToLogger(Logger logger) {
        if (hasMessage) {
            logger.log(messageLevel, getMessage());
        }
    }

    public String getMessage() {
        if (hasMessage) {
            if (messageArgs == null || messageArgs.length > 0) {
                return Fmt.S(m_message, messageArgs);
            }
            return m_message;
        }
        return null;
    }

    public Level getErrorLevel() {
        return messageLevel;
    }

    public boolean isGood() {
        // good is when an error message was not provided
        return messageLevel == Level.INFO;
    }

    public boolean shouldRetry() {
        return shouldRetry;
    }

    public int getRetryMinutes() {
        return timeMinutes;
    }

    public JobParameters getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(JobParameters jp) {
        jobParameters = jp;
    }
}
