package ht.util.startupframework;

/**
 * Service that is called as a main entry point a server / command its called by the command and expected not to return
 * till the vm is expected to exit
 *
 * @author chris
 */
public abstract class RunnableService {
    /**
     * Some services are directly related to commands/ represent a command. Not all services actually should implement
     * run.
     *
     * @return
     */
    public abstract String run();
}
