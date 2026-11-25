package SocialNetwork.utils.events;

/**
 * Enumeration describing the type of change performed on an entity.
 */
public enum ChangeEventType {
    /**
     * A new entity was added.
     */
    ADD,

    /**
     * An existing entity was updated.
     */
    UPDATE,

    /**
     * An existing entity was deleted.
     */
    DELETE
}
