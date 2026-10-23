package Validation;

/**
 * Generic interface for entity validation.
 *
 * @param <E> the type of the entity to validate
 */
public interface Validator<E> {
    /**
     * Validates a given entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is not valid according to domain rules
     */
    void validate(E entity) throws ValidationException;
}
