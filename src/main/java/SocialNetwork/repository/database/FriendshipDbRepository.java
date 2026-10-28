package SocialNetwork.repository.database;

import SocialNetwork.validation.Validator;
import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.repository.Repository;

import java.util.Optional;

/**
 * Database repository implementation for managing {@link Friendship} entities.
 */
public class FriendshipDbRepository implements Repository<Tuple<Long, Long>, Friendship> {
    private final Validator<Friendship> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructs a {@code FriendshipDbRepository} with the given validator and database credentials.
     *
     * @param validator the validator for friendship entities
     * @param url       the database connection URL
     * @param username  the database username
     * @param password  the database password
     */
    public FriendshipDbRepository(Validator<Friendship> validator, String url, String username, String password) {
        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Finds a friendship entity by its identifier.
     *
     * @param id the identifier of the friendship to find
     * @return an {@link Optional} containing the friendship if found, or {@link Optional#empty()} otherwise
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Returns all friendships from the database.
     *
     * @return an iterable collection of all friendships
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Iterable<Friendship> findAll() {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Saves a friendship entity to the database.
     *
     * @param entity the friendship entity to save
     * @return an {@link Optional} containing the existing entity if already present, or {@link Optional#empty()}
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Friendship> save(Friendship entity) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }

    /**
     * Deletes a friendship entity by its identifier from the database.
     *
     * @param id the identifier of the friendship to delete
     * @return an {@link Optional} containing the deleted entity if removed, or {@link Optional#empty()}
     * @throws UnsupportedOperationException because database operations are not yet implemented
     */
    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException("Not implemented yet for Database");
    }
}
