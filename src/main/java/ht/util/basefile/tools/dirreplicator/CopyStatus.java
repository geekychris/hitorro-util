package ht.util.basefile.tools.dirreplicator;

/**
 *
 */
public enum CopyStatus {
    Copied(true, true), AlreadyCopied(true, false), SourceMissing(false, false), TargetNewerThanSource(false, true), MaxRetriesReached(false, false);

    private boolean success;
    private boolean retry;

    CopyStatus(boolean succeeded, boolean retry) {
        this.success = succeeded;
        this.retry = retry;
    }

    public boolean success() {
        return success;
    }

    public boolean shouldRetry() {
        return retry;
    }
}
