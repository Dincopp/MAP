package repository;

import Validation.ValidationException;
import domain.Entity;
import java.util.Optional;

/**
 * Generic CRUD interface for managing entities.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of the entity managed, extending {@link Entity}
 */
public interface Repository<ID, E extends Entity<ID>> {
    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier of the entity to find; cannot be {@code null}
     * @return an {@link Optional} containing the entity if found, or {@link Optional#empty()} otherwise
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all entities from the repository.
     *
     * @return an iterable collection of all entities
     */
    Iterable<E> findAll();

    /**
     * Saves an entity in the repository.
     *
     * @param entity the entity to be saved; cannot be {@code null}
     * @return {@link Optional#empty()} if the entity was saved successfully,
     *         or an {@link Optional} containing the entity if it already exists
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException      if the entity is not valid
     */
    Optional<E> save(E entity);

    /**
     * Deletes the entity identified by ID from the repository.
     *
     * @param id the identifier of the entity to delete; cannot be {@code null}
     * @return an {@link Optional} containing the deleted entity if found and removed,
     *         or {@link Optional#empty()} if no entity exists with the given identifier
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    Optional<E> delete(ID id);
}
