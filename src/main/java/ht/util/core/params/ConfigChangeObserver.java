package ht.util.core.params;

/**
 * Observer to be called back with configuration changes; User: chris
 */
public interface ConfigChangeObserver {
    public void change(ConfigChange cc);

    public void finished();
}
