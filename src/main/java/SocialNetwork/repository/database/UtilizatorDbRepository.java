package SocialNetwork.repository.database;

import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.PagingRepository;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.repository.util.Page;
import SocialNetwork.repository.util.Sort;
import SocialNetwork.validation.Validator;

import SocialNetwork.repository.util.Pageable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database repository implementation for managing {@link Utilizator} entities via PostgreSQL.
 */
public class UtilizatorDbRepository implements PagingRepository<Long, Utilizator> {
    private final Validator<Utilizator> validator;
    private final String url;
    private final String user;
    private final String password;

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
     * @throws IllegalArgumentException if the given ID is null
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilizator u = new Utilizator(
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                    u.setId(rs.getLong("id"));
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding user by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Returns all users from the database.
     *
     * @return an iterable collection of all users
     */
    @Override
    public Iterable<Utilizator> findAll() {
        List<Utilizator> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utilizator u = new Utilizator(
                        rs.getString("first_name"),
                        rs.getString("last_name")
                );
                u.setId(rs.getLong("id"));
                users.add(u);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding all users: " + e.getMessage(), e);
        }

        return users;
    }

    /**
     * Saves a user entity to the database. If ID generation is supported by the DB, the generated key is assigned to the entity.
     *
     * @param utilizator the user entity to save
     * @return an {@link Optional} containing the existing user if already present, or {@link Optional#empty()}
     * @throws IllegalArgumentException if the entity is null
     */
    @Override
    public Optional<Utilizator> save(Utilizator utilizator) {
        if (utilizator == null) {
            throw new IllegalArgumentException("User cannot be null!");
        }
        validator.validate(utilizator);

        String sql = "INSERT INTO users (first_name, last_name) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, utilizator.getFirstName());
            ps.setString(2, utilizator.getLastName());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilizator.setId(generatedKeys.getLong(1));
                    System.out.println("Auto-generated id:"+utilizator.getId());
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Error inserting user into db: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a user entity by its identifier from the database.
     *
     * @param entity the identifier of the user to delete
     * @return an {@link Optional} containing the updated entity, or {@link Optional#empty()}
     * @throws IllegalArgumentException if the entity is null
     */

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
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

        String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());
            statement.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la actualizarea utilizatorului", e);
        }
    }

    /**
     * Deletes a user entity by its identifier from the database.
     *
     * @param id the identifier of the user to delete
     * @return an {@link Optional} containing the deleted entity if removed, or {@link Optional#empty()}
     * @throws IllegalArgumentException if the ID is null
     */
    @Override
    public Optional<Utilizator> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }

        Optional<Utilizator> userToDelete = findOne(id);
        if (userToDelete.isEmpty()) {
            return Optional.empty();
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

            return userToDelete;
        } catch (SQLException e) {
            throw new RepositoryException("Error deleting user from db: " + e.getMessage(), e);
        }
    }


    private int count(Connection connection) throws SQLException{
        String sql = "SELECT COUNT(*) AS count FROM users";
        try(PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()){
            int numberUtilizator=0;
            while(resultSet.next()){
                numberUtilizator = resultSet.getInt("count");
            }
            return numberUtilizator;
            
        }
    }

    public Page<Utilizator> findAllOnPage(Pageable pageable){
        List<Utilizator> utilizatori = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id LIMIT ? OFFSET ?";

        try(Connection connection = DriverManager.getConnection(url,user,password);
        PreparedStatement statement = connection.prepareStatement(sql)){
            int totalUtilizatori = count(connection);
            statement.setInt(1,pageable.getPageSize());
            statement.setInt(2,pageable.getPageSize()*pageable.getPageNumber());
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    Long id = resultSet.getLong("id");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    Utilizator u = new Utilizator(first_name,last_name);
                    u.setId(id);
                    utilizatori.add(u);
                }
            }
            return new Page<>(utilizatori,totalUtilizatori);
        }catch(SQLException e){
            throw new RepositoryException("Error: "+ e.getMessage(),e);
        }
    }

}
