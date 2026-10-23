package repository.memory;

import domain.Entity;
import Validation.Validator;
import Validation.ValidationException;
import repository.Repository;
import repository.RepositoryException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the {@link Repository} interface, using a {@link Map}.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of the entity, extending {@link Entity}
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private final Validator<E> validator;
    protected Map<ID, E> entities = new HashMap<>();

    /**
     * Constructs an in-memory repository using the specified entity validator.
     *
     * @param validator the validator used to verify entities
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
    }

    /**
     * Finds an entity in memory by its identifier.
     *
     * @param id the identifier of the entity to find
     * @return an {@link Optional} containing the entity if found, or {@link Optional#empty()} otherwise
     * @throws RepositoryException if the identifier is null
     */
    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new RepositoryException("ID cannot be null!");
        }
        return Optional.ofNullable(entities.get(id));
    }

    /**
     * Returns all entities stored in memory.
     *
     * @return an {@link Iterable} over all entities in memory
     */
    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    /**
     * Validates and saves an entity in memory.
     *
     * @param entity the entity to save
     * @return {@link Optional#empty()} if the entity was saved successfully,
     *         or an {@link Optional} containing the entity if it already exists
     * @throws RepositoryException if the entity is null
     * @throws ValidationException if the entity is not valid according to the validator
     */
    @Override
    public Optional<E> save(E entity) {
        if (entity == null) {
            throw new RepositoryException("Entity cannot be null!");
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            return Optional.of(entity);
        }
        entities.put(entity.getId(), entity);
        return Optional.empty();
    }

    /**
     * Deletes an entity from memory by its identifier.
     *
     * @param id the identifier of the entity to delete
     * @return an {@link Optional} containing the deleted entity if found, or {@link Optional#empty()} otherwise
     * @throws RepositoryException if the identifier is null
     */
    @Override
    public Optional<E> delete(ID id) {
        if (id == null) {
            throw new RepositoryException("ID cannot be null!");
        }
        return Optional.ofNullable(entities.remove(id));
    }

    /**
     * Checks whether the repository is empty.
     *
     * @return {@code true} if the repository contains no entities, {@code false} otherwise
     */
    public boolean Empty() {
        return entities.isEmpty();
    }
}
