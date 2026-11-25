package SocialNetwork.repository;

import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.validation.Validator;
import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.database.CererePrietenieDbRepository;
import SocialNetwork.repository.database.FriendshipDbRepository;
import SocialNetwork.repository.database.UtilizatorDbRepository;
import SocialNetwork.repository.file.FriendshipFileRepository;
import SocialNetwork.repository.file.UtilizatorFileRepository;
import SocialNetwork.repository.memory.FriendshipRepository;
import SocialNetwork.repository.memory.InMemoryRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Factory class responsible for creating repository instances based on configuration properties.
 * Utilizes a functional {@link Map} dispatch pattern with {@link Function} creators
 * to eliminate imperative switch statements.
 */
public class RepositoryFactory {
    private static final int DEFAULT_PAGE_SIZE = 5;

    private final Properties properties = new Properties();
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Long, Utilizator>>> userRepoCreators = new EnumMap<>(RepositoryType.class);
    private final Map<RepositoryType, Function<Validator<Friendship>, Repository<Tuple<Long, Long>, Friendship>>> friendshipRepoCreators = new EnumMap<>(RepositoryType.class);
    private final Map<RepositoryType, Function<Validator<CererePrietenie>, Repository<Long, CererePrietenie>>> friendRequestRepoCreators = new EnumMap<>(RepositoryType.class);

    /**
     * Constructs a {@code RepositoryFactory} and loads configuration properties from the resource file,
     * registering repository creation functions.
     *
     * @throws RepositoryException if the configuration file cannot be found or loaded
     */
    public RepositoryFactory() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RepositoryException("Could not find config.properties in resources.");
            }
            properties.load(input);
        } catch (IOException | IllegalArgumentException e) {
            throw new RepositoryException("Failed to load configuration: " + e.getMessage());
        }

        initCreators();
    }

    /**
     * Initializes repository creator maps using functional mappings and method references.
     */
    private void initCreators() {
        // User repository creators
        userRepoCreators.put(RepositoryType.MEMORY, InMemoryRepository::new);
        userRepoCreators.put(RepositoryType.FILE, validator -> {
            String filename = properties.getProperty("users.file");
            if (filename == null || filename.trim().isEmpty()) {
                throw new RepositoryException("File name not specified for users!");
            }
            return new UtilizatorFileRepository(validator, filename);
        });
        userRepoCreators.put(RepositoryType.DATABASE, validator -> {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            if (url == null || user == null || password == null) {
                throw new RepositoryException("Incomplete connection details! (url, user, password)");
            }
            return new UtilizatorDbRepository(validator, url, user, password);
        });

        // Friendship repository creators
        friendshipRepoCreators.put(RepositoryType.MEMORY, FriendshipRepository::new);
        friendshipRepoCreators.put(RepositoryType.FILE, validator -> {
            String filename = properties.getProperty("friendships.file");
            if (filename == null || filename.trim().isEmpty()) {
                throw new RepositoryException("File name not specified for friendships!");
            }
            return new FriendshipFileRepository(validator, filename);
        });
        friendshipRepoCreators.put(RepositoryType.DATABASE, validator -> {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            if (url == null || user == null || password == null) {
                throw new RepositoryException("Incomplete connection details! (url, user, password)");
            }
            return new FriendshipDbRepository(validator, url, user, password);
        });

        // Friend request repository creators
        friendRequestRepoCreators.put(RepositoryType.MEMORY, InMemoryRepository::new);
        friendRequestRepoCreators.put(RepositoryType.FILE, InMemoryRepository::new);
        friendRequestRepoCreators.put(RepositoryType.DATABASE, validator -> {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            if (url == null || user == null || password == null) {
                throw new RepositoryException("Incomplete connection details! (url, user, password)");
            }
            return new CererePrietenieDbRepository(validator, url, user, password);
        });
    }

    /**
     * Resolves the configured {@link RepositoryType} from properties.
     *
     * @return the configured {@link RepositoryType}
     * @throws RepositoryException if the property is missing or invalid
     */
    private RepositoryType getRepositoryType() {
        String typeStr = properties.getProperty("repository.type");
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new RepositoryException("Property repository.type is empty!");
        }
        try {
            return RepositoryType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Invalid repository type: " + typeStr);
        }
    }

    /**
     * Creates a user repository according to the configured repository type.
     *
     * @param validator the validator used for validating user entities
     * @return the configured user {@link Repository}
     * @throws RepositoryException if the repository type is unsupported or creation fails
     */
    public Repository<Long, Utilizator> createUserRepository(Validator<Utilizator> validator) {
        RepositoryType repoType = getRepositoryType();
        Function<Validator<Utilizator>, Repository<Long, Utilizator>> creator = userRepoCreators.get(repoType);
        if (creator == null) {
            throw new RepositoryException("Unknown repository type: " + repoType);
        }
        return creator.apply(validator);
    }

    /**
     * Creates a friendship repository according to the configured repository type.
     *
     * @param validator the validator used for validating friendship entities
     * @return the configured friendship {@link Repository}
     * @throws RepositoryException if the repository type is unsupported or creation fails
     */
    public Repository<Tuple<Long, Long>, Friendship> createFriendshipRepository(Validator<Friendship> validator) {
        RepositoryType repoType = getRepositoryType();
        Function<Validator<Friendship>, Repository<Tuple<Long, Long>, Friendship>> creator = friendshipRepoCreators.get(repoType);
        if (creator == null) {
            throw new RepositoryException("Unknown repository type: " + repoType);
        }
        return creator.apply(validator);
    }

    /**
     * Creates a friend request repository according to the configured repository type.
     *
     * @param validator the validator used for validating {@link CererePrietenie} entities
     * @return the configured friend request {@link Repository}
     * @throws RepositoryException if the repository type is unsupported or creation fails
     */
    public Repository<Long, CererePrietenie> createFriendRequestRepository(Validator<CererePrietenie> validator) {
        RepositoryType repoType = getRepositoryType();
        Function<Validator<CererePrietenie>, Repository<Long, CererePrietenie>> creator = friendRequestRepoCreators.get(repoType);
        if (creator == null) {
            throw new RepositoryException("Unknown repository type: " + repoType);
        }
        return creator.apply(validator);
    }

    /**
     * Reads the configured page size from {@code config.properties}.
     *
     * @return the configured page size, or {@code 5} if missing or invalid
     */
    public int getPageSize() {
        String raw = properties.getProperty("PAGE_SIZE");
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }
}
