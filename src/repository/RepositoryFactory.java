package repository;

import Validation.Validator;
import domain.Friendship;
import domain.Tuple;
import domain.Utilizator;
import repository.database.FriendshipDbRepository;
import repository.database.UtilizatorDbRepository;
import repository.file.FriendshipFileRepository;
import repository.file.UtilizatorFileRepository;
import repository.memory.FriendshipRepository;
import repository.memory.InMemoryRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory class responsible for creating repository instances based on configuration properties.
 */
public class RepositoryFactory {
    private final Properties properties = new Properties();

    /**
     * Constructs a {@code RepositoryFactory} and loads configuration properties from the resource file.
     *
     * @throws RepositoryException if the configuration file cannot be found or loaded
     */
    public RepositoryFactory() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("main/resources/config.properties")) {
            if (input == null) {
                throw new RepositoryException("Could not find config.properties in resources.");
            }
            properties.load(input);
        } catch (IOException | IllegalArgumentException e) {
            throw new RepositoryException("Failed to load configuration: " + e.getMessage());
        }
    }

    /**
     * Creates a user repository according to the configured repository type.
     *
     * @param validator the validator used for validating user entities
     * @return the configured user {@link Repository}
     * @throws RepositoryException if the configuration properties are missing, invalid, or incomplete
     */
    public Repository<Long, Utilizator> createUserRepository(Validator<Utilizator> validator) {
        if (properties == null) {
            throw new RepositoryException("Properties cannot be null!");
        }

        String typeStr = properties.getProperty("repository.type");
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new RepositoryException("Property repository.type is empty!");
        }

        RepositoryType repoType;

        try {
            repoType = RepositoryType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException | RepositoryException e) {
            throw new RepositoryException("Invalid repository type: " + typeStr);
        }

        switch (repoType) {
            case MEMORY:
                return new InMemoryRepository<>(validator);

            case FILE:
                String filename = properties.getProperty("users.file");
                if (filename == null || filename.trim().isEmpty()) {
                    throw new RepositoryException("File name not specified!");
                }
                return new UtilizatorFileRepository(validator, filename);

            case DATABASE:
                String url = properties.getProperty("db.url");
                String user = properties.getProperty("db.user");
                String password = properties.getProperty("db.password");

                if (url == null || user == null || password == null) {
                    throw new RepositoryException("Incomplete connection details! (url, user, password)");
                }
                return new UtilizatorDbRepository(validator, url, user, password);

            default:
                throw new RepositoryException("Unknown repository type.");
        }
    }

    /**
     * Creates a friendship repository according to the configured repository type.
     *
     * @param validator the validator used for validating friendship entities
     * @return the configured friendship {@link Repository}
     * @throws RepositoryException if the configuration properties are missing, invalid, or incomplete
     */
    public Repository<Tuple<Long, Long>, Friendship> createFriendshipRepository(Validator<Friendship> validator) {
        if (properties == null) {
            throw new RepositoryException("Properties cannot be null!");
        }
        String strType = properties.getProperty("repository.type");
        if (strType == null || strType.trim().isEmpty()) {
            throw new RepositoryException("Property repository.type is empty!");
        }

        RepositoryType repoType;

        try {
            repoType = RepositoryType.valueOf(strType.trim().toUpperCase());
        } catch (IllegalArgumentException | RepositoryException e) {
            throw new RepositoryException("Invalid repository type: " + strType);
        }

        switch (repoType) {
            case MEMORY:
                return new FriendshipRepository(validator);
            case FILE:
                String filename = properties.getProperty("friendships.file");
                if (filename == null || filename.trim().isEmpty()) {
                    throw new RepositoryException("File name not specified!");
                }
                return new FriendshipFileRepository(validator, filename);
            case DATABASE:
                String url = properties.getProperty("db.url");
                String user = properties.getProperty("db.user");
                String password = properties.getProperty("db.password");
                if (url == null || user == null || password == null) {
                    throw new RepositoryException("Incomplete connection details! (url, user, password)");
                }
                return new FriendshipDbRepository(validator, url, user, password);
            default:
                throw new RepositoryException("Unknown repository type: " + repoType);
        }
    }
}
