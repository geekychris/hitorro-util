package ht.util.core.thread;

/**
 * Optional interface a runnable can implement IF it wants to integrate more effectively with RestartableDaemon.
 * Primarilly there are two features:
 * <p/>
 * - Ability to tell the daemon when it should no longer be restarted (such as its done!) - close mechanism that called
 * once completed User: chris
 */
public interface RestartableRunnableIntf {
    boolean isFinished();

    void closeRestartable();
}