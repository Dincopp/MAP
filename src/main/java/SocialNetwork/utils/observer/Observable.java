package SocialNetwork.utils.observer;

import SocialNetwork.utils.events.Event;

/**
 * Observable interface implemented by services or models that notify registered {@link Observer}s
 * whenever their state changes.
 *
 * @param <E> the type of {@link Event} emitted by this observable
 */
public interface Observable<E extends Event> {
    /**
     * Registers an observer to receive change notifications.
     *
     * @param observer the observer to register
     */
    void addObserver(Observer<E> observer);

    /**
     * Unregisters a previously registered observer.
     *
     * @param observer the observer to remove
     */
    void removeObserver(Observer<E> observer);

    /**
     * Notifies all registered observers about an event.
     *
     * @param event the event to broadcast
     */
    void notifyObservers(E event);
}
