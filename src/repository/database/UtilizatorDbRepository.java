package repository.database;

import Validation.Validator;
import domain.Utilizator;
import repository.Repository;

import java.util.Optional;

/**
 * Database repository implementation for managing {@link Utilizator} entities.
 */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private Validator<Utilizator> validator;
    private String url;
    private String user;
    private String password;

    /**
     * Constructs a {@code UtilizatorDbRepository} with the given validator and database credentials.
     *
     * @param validator the validator for user entities
     * @param url       the database connection URL
     * @param user      the database username
     * @param password  the database password
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Finds a user entity by its identifier.
     *
     * @param id the identifier of the user to find
     * @return an {@link Optional} containing the user if found, or {@link Optional#empty()} otherwise
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Returns all users from the database.
     *
     * @return an iterable collection of all users
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Iterable<Utilizator> findAll() {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Saves a user entity to the database.
     *
     * @param utilizator the user entity to save
     * @return an {@link Optional} containing the existing user if already present, or {@link Optional#empty()}
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Utilizator> save(Utilizator utilizator) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Deletes a user entity by its identifier from the database.
     *
     * @param id the identifier of the user to delete
     * @return an {@link Optional} containing the deleted entity if removed, or {@link Optional#empty()}
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Utilizator> delete(Long id) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }
}
