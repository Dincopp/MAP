package SocialNetwork.validation;

import java.util.Objects;

/**
 * Generic functional interface for entity validation.
 *
 * @param <E> the type of the entity to validate
 */
@FunctionalInterface
public interface Validator<E> {
    /**
     * Validates a given entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is not valid according to domain rules
     */
    void validate(E entity) throws ValidationException;

    /**
     * Returns a composed {@code Validator} that performs, in sequence, this
     * operation followed by the {@code other} operation.
     *
     * @param other the other validator to perform after this one; cannot be {@code null}
     * @return a composed {@code Validator} that performs sequentially this operation followed by {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Validator<E> andThen(Validator<E> other) {
        Objects.requireNonNull(other, "The other validator cannot be null");
        return entity -> {
            this.validate(entity);
            other.validate(entity);
        };
    }
}
