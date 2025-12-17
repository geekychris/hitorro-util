package ht.util.core.events;

/**
 * Interface that is implemented by someone that requires to be notified on some topic by the event hub by a caller of
 * LocalEventHub.addEvent
 *
 * @author chris
 * @see LocalEventHub
 */
public interface EventListener {
    /**
     * method to notify an observers of an event.
     *
     * @param topic
     * @param subTopic
     * @param args
     * @return true if delivered.
     */
    boolean event(String topic, String subTopic, Object args);

    /**
     * @return Name of the event
     */
    String eventName();

    /**
     * Event me using a separate thread.
     *
     * @return true => async
     */
    boolean runAsync();
}
