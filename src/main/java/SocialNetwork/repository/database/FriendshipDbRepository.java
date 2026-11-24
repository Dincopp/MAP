package SocialNetwork.repository.database;

import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.PagingRepository;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.repository.util.Page;
import SocialNetwork.validation.Validator;
import SocialNetwork.repository.util.Pageable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database repository implementation for managing {@link Friendship} entities via PostgreSQL,
 * supporting pagination via {@link PagingRepository}.
 */
public class FriendshipDbRepository implements PagingRepository<Tuple<Long, Long>, Friendship> {
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
     * Finds a friendship entity by its normalized tuple identifier.
     *
     * @param id the identifier of the friendship to find; cannot be null
     * @return an {@link Optional} containing the friendship if found, or {@link Optional#empty()} otherwise
     * @throws IllegalArgumentException if the identifier is null
     */
    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        if (id == null) {
            throw new IllegalArgumentException("Friendship ID cannot be null!");
        }

        Long minId = Math.min(id.getLeft(), id.getRight());
        Long maxId = Math.max(id.getLeft(), id.getRight());

        String sql = "SELECT * FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, minId);
            ps.setLong(2, maxId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("friends_from");
                    LocalDateTime date = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    Friendship f = new Friendship(minId, maxId, date);
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding friendship by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Returns all friendships from the database.
     *
     * @return an iterable collection of all friendships
     */
    @Override
    public Iterable<Friendship> findAll() {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships ORDER BY id1, id2";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id1 = rs.getLong("id1");
                Long id2 = rs.getLong("id2");
                Timestamp ts = rs.getTimestamp("friends_from");
                LocalDateTime date = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                friendships.add(new Friendship(id1, id2, date));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding all friendships: " + e.getMessage(), e);
        }

        return friendships;
    }

    /**
     * Saves a friendship entity to the database.
     *
     * @param entity the friendship entity to save
     * @return {@link Optional#empty()} if saved successfully, or an {@link Optional} containing the entity if it already exists
     * @throws IllegalArgumentException if the entity is null
     */
    @Override
    public Optional<Friendship> save(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friendship entity cannot be null!");
        }
        validator.validate(entity);

        Optional<Friendship> existing = findOne(entity.getId());
        if (existing.isPresent()) {
            return existing;
        }

        String sql = "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getIdUser1());
            ps.setLong(2, entity.getIdUser2());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            ps.executeUpdate();

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Error saving friendship into db: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing friendship entity in the database.
     *
     * @param entity the friendship entity with updated values; cannot be null and must have a valid ID
     * @return {@link Optional#empty()} if the friendship was updated successfully,
     *         or an {@link Optional} containing the entity if it does not exist
     * @throws IllegalArgumentException if the entity or its ID is null
     * @throws ValidationException      if the entity is not valid according to the validator
     * @throws RepositoryException      if a database error occurs
     */
    @Override
    public Optional<Friendship> update(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("ID-ul entitatii nu poate fi null pentru update!");
        }
        validator.validate(entity);

        if (findOne(entity.getId()).isEmpty()) {
            return Optional.of(entity); // Intoarce entitatea daca nu exista
        }

        String sql = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(entity.getDate()));
            statement.setLong(2, entity.getId().getLeft());
            statement.setLong(3, entity.getId().getRight());

            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la actualizarea prieteniei", e);
        }
    }

    /**
     * Deletes a friendship entity by its identifier from the database.
     *
     * @param id the identifier of the friendship to delete
     * @return an {@link Optional} containing the deleted entity if removed, or {@link Optional#empty()}
     * @throws IllegalArgumentException if the ID is null
     */
    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        if (id == null) {
            throw new IllegalArgumentException("Friendship ID cannot be null!");
        }

        Optional<Friendship> friendshipToDelete = findOne(id);
        if (friendshipToDelete.isEmpty()) {
            return Optional.empty();
        }

        Long minId = Math.min(id.getLeft(), id.getRight());
        Long maxId = Math.max(id.getLeft(), id.getRight());

        String sql = "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, minId);
            ps.setLong(2, maxId);
            ps.executeUpdate();

            return friendshipToDelete;
        } catch (SQLException e) {
            throw new RepositoryException("Error deleting friendship from db: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a page of {@link Friendship} entities from the database based on the pagination criteria.
     *
     * @param pageable pagination specification defining page number and page size
     * @return a {@link Page} containing the friendships on the requested page and the total count of friendships
     * @throws RepositoryException if a database access error occurs during query execution
     */
    @Override
    public Page<Friendship> findAllOnPage(Pageable pageable) {
        List<Friendship> prietenii = new ArrayList<>();
        String sql = "SELECT id1, id2, friends_from FROM friendships ORDER BY id1, id2 LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM friendships";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            int total = 0;
            try (PreparedStatement countStmt = connection.prepareStatement(countSql);
                 ResultSet rsCount = countStmt.executeQuery()) {
                if (rsCount.next()) {
                    total = rsCount.getInt(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, pageable.getPageSize());
                statement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Long id1 = resultSet.getLong("id1");
                        Long id2 = resultSet.getLong("id2");
                        LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();

                        Friendship f = new Friendship(id1, id2, friendsFrom);
                        prietenii.add(f);
                    }
                }
            }
            return new Page<>(prietenii, total);
        } catch (SQLException e) {
            throw new RepositoryException("Error: " + e.getMessage(), e);
        }
    }
}
