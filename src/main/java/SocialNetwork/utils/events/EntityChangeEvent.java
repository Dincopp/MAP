package SocialNetwork.utils.events;

/**
 * Event representing a state modification (add, update, or delete) on an entity.
 *
 * @param <E> the type of entity involved in the change
 */
public class EntityChangeEvent<E> implements Event {
    private final ChangeEventType type;
    private final E data;
    private final E oldData;

    /**
     * Constructs an {@code EntityChangeEvent} with the change type and the affected entity.
     *
     * @param type the type of modification
     * @param data the new or affected entity state
     */
    public EntityChangeEvent(ChangeEventType type, E data) {
        this(type, data, null);
    }

    /**
     * Constructs an {@code EntityChangeEvent} including both the new and previous entity states.
     *
     * @param type    the type of modification
     * @param data    the new entity state
     * @param oldData the previous entity state
     */
    public EntityChangeEvent(ChangeEventType type, E data, E oldData) {
        this.type = type;
        this.data = data;
        this.oldData = oldData;
    }

    /**
     * Returns the type of change that occurred.
     *
     * @return the {@link ChangeEventType}
     */
    public ChangeEventType getType() {
        return type;
    }

    /**
     * Returns the affected entity data.
     *
     * @return the current or newly saved entity
     */
    public E getData() {
        return data;
    }

    /**
     * Returns the previous entity state, if applicable.
     *
     * @return the previous entity state, or {@code null}
     */
    public E getOldData() {
        return oldData;
    }
}
