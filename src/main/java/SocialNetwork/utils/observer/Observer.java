package SocialNetwork.utils.observer;

import SocialNetwork.utils.events.Event;

/**
 * Observer interface for components listening to state change events.
 *
 * @param <E> the type of {@link Event} handled by this observer
 */
public interface Observer<E extends Event> {
    /**
     * Called when an observed {@link Observable} notifies its listeners of an event.
     *
     * @param event the event describing the change
     */
    void update(E event);
}
