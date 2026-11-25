package SocialNetwork.repository.database;

import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.domain.FriendRequestStatus;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.validation.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC database repository implementation for managing {@link CererePrietenie} entities
 * persisted in the PostgreSQL {@code friend_requests} table.
 */
public class CererePrietenieDbRepository implements Repository<Long, CererePrietenie> {
    private final Validator<CererePrietenie> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructs a {@code CererePrietenieDbRepository} with the specified validator and database credentials.
     *
     * @param validator the validator used to validate friend request entities
     * @param url       the JDBC database URL
     * @param username  the database username
     * @param password  the database password
     */
    public CererePrietenieDbRepository(Validator<CererePrietenie> validator, String url, String username, String password) {
        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Maps the current row of a {@link ResultSet} into a {@link CererePrietenie} entity.
     *
     * @param rs the result set positioned at a valid row
     * @return the mapped {@link CererePrietenie} entity
     * @throws SQLException if a column lookup fails
     */
    private CererePrietenie extractEntity(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long fromId = rs.getLong("from_id");
        Long toId = rs.getLong("to_id");
        FriendRequestStatus status = FriendRequestStatus.valueOf(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("date");
        LocalDateTime date = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        CererePrietenie request = new CererePrietenie(fromId, toId, status, date);
        request.setId(id);
        return request;
    }

    /**
     * Finds a friend request by its unique identifier.
     *
     * @param id the identifier of the friend request to find; cannot be {@code null}
     * @return an {@link Optional} containing the found entity, or {@link Optional#empty()} if not found
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Optional<CererePrietenie> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Friend request ID cannot be null!");
        }

        String sql = "SELECT id, from_id, to_id, status, date FROM friend_requests WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding friend request by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Returns all friend requests stored in the database, ordered by ID.
     *
     * @return an {@link Iterable} containing all {@link CererePrietenie} entities
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Iterable<CererePrietenie> findAll() {
        List<CererePrietenie> requests = new ArrayList<>();
        String sql = "SELECT id, from_id, to_id, status, date FROM friend_requests ORDER BY id";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requests.add(extractEntity(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding all friend requests: " + e.getMessage(), e);
        }

        return requests;
    }

    /**
     * Validates and persists a new friend request in the database, assigning the generated ID.
     *
     * @param entity the friend request to save; cannot be {@code null}
     * @return {@link Optional#empty()} if the entity was saved successfully
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws RepositoryException      if a database error occurs
     */
    @Override
    public Optional<CererePrietenie> save(CererePrietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friend request entity cannot be null!");
        }
        validator.validate(entity);

        String sql = "INSERT INTO friend_requests (from_id, to_id, status, date) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getFrom());
            ps.setLong(2, entity.getTo());
            ps.setString(3, entity.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Error saving friend request to database: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing friend request in the database.
     *
     * @param entity the friend request with updated values; cannot be {@code null} and must have a non-null ID
     * @return {@link Optional#empty()} if updated successfully, or an {@link Optional} containing {@code entity} if not found
     * @throws IllegalArgumentException if {@code entity} or its ID is {@code null}
     * @throws RepositoryException      if a database error occurs
     */
    @Override
    public Optional<CererePrietenie> update(CererePrietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friend request entity cannot be null!");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Friend request ID cannot be null for update!");
        }
        validator.validate(entity);

        if (findOne(entity.getId()).isEmpty()) {
            return Optional.of(entity);
        }

        String sql = "UPDATE friend_requests SET from_id = ?, to_id = ?, status = ?, date = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getFrom());
            ps.setLong(2, entity.getTo());
            ps.setString(3, entity.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            ps.setLong(5, entity.getId());
            ps.executeUpdate();

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Error updating friend request in database: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a friend request from the database by its identifier.
     *
     * @param id the identifier of the friend request to delete; cannot be {@code null}
     * @return an {@link Optional} containing the deleted entity if found, or {@link Optional#empty()} otherwise
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if a database error occurs
     */
    @Override
    public Optional<CererePrietenie> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Friend request ID cannot be null!");
        }

        Optional<CererePrietenie> existing = findOne(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        String sql = "DELETE FROM friend_requests WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepositoryException("Error deleting friend request from database: " + e.getMessage(), e);
        }
    }
}
