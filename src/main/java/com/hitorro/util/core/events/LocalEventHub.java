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
package com.hitorro.util.core.events;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.thread.EnhancedThreadFactory;
import com.hitorro.util.core.thread.EnhancedThreadGroup;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The hub is responsible for two things:
 * <p/>
 * 1) one registers event listeners (callbacks that listen for events that occur) 2) take events from event producers
 * and distribute them to registered event listeners (consumers).
 * <p/>
 * One should probably create one of these in a running system, unless you want to keep a bunch of private communication
 * buses.
 *
 * @author chris
 */
public class LocalEventHub implements EventListener {
    public static final String Name = "LocalEventHubAsyncNotifiier";
    public static final EnhancedThreadGroup s_tg = new EnhancedThreadGroup(Name);
    private static final LocalEventHub s_localHub = new LocalEventHub();
    public ExecutorService m_executors;
    private HashMap<String, WeakReferenceList<EventListener>> type =
            new HashMap<String, WeakReferenceList<EventListener>>();

    public LocalEventHub() {
        initThreadPool();
    }

    public static final LocalEventHub get() {
        return s_localHub;
    }

    /**
     * Get listFiles of registered listeners
     *
     * @return list of registered listeners
     */
    public List<GenericKeyValue> getRegisteredListeners() {
        List<GenericKeyValue> kvList = new ArrayList<GenericKeyValue>();

        Iterator<Map.Entry<String, WeakReferenceList<EventListener>>> iter = type.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, WeakReferenceList<EventListener>> entry = iter.next();
            WeakReferenceList<EventListener> wl = entry.getValue();
            String key = entry.getKey();
            for (int i = wl.size() - 1; i >= 0; i--) {
                EventListener el = wl.get(i);
                kvList.add(new GenericKeyValue(key, el.eventName()));
            }
        }
        return kvList;
    }

    public String eventName() {
        return "Local Hub";
    }


    /**
     * Determine if a topic has something listening on it at the moment.
     *
     * @param topic
     * @return
     */
    public synchronized boolean hasEventRegistered(String topic) {
        return type.get(topic) != null;
    }

    /**
     * Called by someone that whishes to notify this process of an event.  All Observers of this event are notified.
     */
    public synchronized List<String> eventListingNotified(String eventName, String subEvent, List args) {
        List<String> notified = new ArrayList<String>();
        WeakReferenceList<EventListener> l = type.get(eventName);
        boolean hasNulls = false;
        if (l != null) {
            synchronized (l) {
                // this is a course lock to take.
                int size = l.size();
                for (int i = 0; i < size; i++) {
                    EventListener el = l.get(i);
                    if (el == null) {
                        // we need to clean up nulls later
                        hasNulls = true;
                    } else {
                        notified.add(el.eventName());
                        executeEvent(eventName, subEvent, args, el);
                    }
                }
                if (hasNulls) {
                    l.removeNulls();
                }
            }
            return notified;
        } else {
            return notified;
        }
    }

    public synchronized boolean event(String eventName, String subEvent, Object args) {
        WeakReferenceList<EventListener> l = type.get(eventName);
        boolean hasNulls = false;
        if (l != null) {
            synchronized (l) {
                int size = l.size();
                for (int i = 0; i < size; i++) {
                    EventListener el = l.get(i);
                    if (el == null) {
                        hasNulls = true;
                    } else {
                        executeEvent(eventName, subEvent, args, el);
                    }
                }
                if (hasNulls) {
                    l.removeNulls();
                }
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Execute an event either synchronously or async (use a seperate thread).
     * <p/>
     * Notes: We should probably introduce a thread pool.  Within the thread pool, active threads should register the
     * event and event listener that they are processing.  We should then be able to determine if we want duplicate
     * events for a given listener to be executed or thrown away.
     *
     * @param eventName
     * @param subEvent
     * @param args
     * @param el
     */
    void executeEvent(String eventName, String subEvent, Object args, EventListener el) {
        if (el.runAsync()) {
            AsyncEventNotification async =
                    new AsyncEventNotification(el,
                            eventName,
                            subEvent,
                            args);
            m_executors.execute(async);
        } else {
            el.event(eventName, subEvent, args);
        }
    }

    /**
     * Communicate to ALL observers that have registered with the LocalEventHub. This implies that Observers MUST ensure
     * that the subevent is a sensible one for them to process.
     *
     * @param subEvent
     * @param args
     */
    public void eventAll(String subEvent, List args) {
        Set set = type.keySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            event(s, subEvent, args);
        }
    }

    /**
     * Registration mechanism for an observer to register against the event hub for notification of events. Note that
     * the event listFiles holds weak references. Therefore, the observer (i.e., the event handler) will disappear unless its
     * reference is held by another object.
     *
     * @param el        instance that honors the EventListenter interface
     * @param eventName that the observer whishes to listen on.
     */
    public synchronized void addEventListener(EventListener el, String eventName) {
        WeakReferenceList<EventListener> l = type.get(eventName);
        if (l == null) {
            l = new WeakReferenceList<EventListener>();
            type.put(eventName, l);
        }

        synchronized (l) {
            l.addIfAbsent(el);
        }
    }

    public boolean runAsync() {
        return false;
    }

    private void initThreadPool() {
        m_executors = Executors.newCachedThreadPool(
                new EnhancedThreadFactory("EventListenerThreadPool",
                        "EventThread(%s)", true));
    }
}

class AsyncEventNotification implements Runnable {
    private EventListener m_el;
    private String m_topic;
    private String m_subTopic;
    private Object m_args;

    public AsyncEventNotification(EventListener el, String topic, String subTopic, Object args) {
        m_el = el;
        m_topic = topic;
        m_subTopic = subTopic;
        m_args = args;
    }

    public void run() {
        m_el.event(m_topic, m_subTopic, m_args);
    }
}
