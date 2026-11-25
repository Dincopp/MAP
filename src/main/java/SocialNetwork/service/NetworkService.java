package SocialNetwork.service;

import SocialNetwork.domain.Entity;
import SocialNetwork.domain.Friendship;
import SocialNetwork.repository.PagingRepository;
import SocialNetwork.repository.database.FriendshipDbRepository;
import SocialNetwork.repository.util.Page;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.memory.FriendshipRepository;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.util.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service responsible for managing social network business logic using Java 8 Stream API:
 * adding/removing users, establishing/removing friendships,
 * and retrieving network-related statistics without imperative loops.
 */
public class NetworkService {
    private final Repository<Long, Utilizator> userRepo;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepo;

    /**
     * Constructs a {@code NetworkService} with a user repository and a friendship repository.
     *
     * @param userRepo       the user repository
     * @param friendshipRepo the friendship repository
     */
    public NetworkService(Repository<Long, Utilizator> userRepo, Repository<Tuple<Long, Long>, Friendship> friendshipRepo) {
        this.userRepo = userRepo;
        this.friendshipRepo = friendshipRepo;
    }

    /**
     * Constructs a {@code NetworkService} with a user repository and a default in-memory friendship repository.
     *
     * @param userRepo the user repository
     */
    public NetworkService(Repository<Long, Utilizator> userRepo) {
        this(userRepo, new FriendshipRepository(new FriendshipValidator()));
    }

    /**
     * Adds a new user to the system. If the user does not have an ID set, one is automatically generated incrementally.
     * Checks for duplicates using {@link java.util.stream.Stream#anyMatch(Predicate)}.
     *
     * @param utilizator the user to add
     * @return the added user
     * @throws ServiceException if the user is null, a user with the same full name exists, or a user with the same ID already exists
     */
    public Utilizator addUser(Utilizator utilizator) {
        if (utilizator == null) {
            throw new ServiceException("User cannot be null!");
        }

        boolean exists = StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .anyMatch(u -> Objects.equals(utilizator.getFirstName(), u.getFirstName()) &&
                               Objects.equals(utilizator.getLastName(), u.getLastName()));

        if (exists) {
            throw new ServiceException("Un utilizator cu acest nume complet exista deja!");
        }

        if (utilizator.getId() == null) {
            utilizator.setId(generateId());
        }

        Optional<Utilizator> existing = userRepo.save(utilizator);
        if (existing.isPresent()) {
            throw new ServiceException("User with ID " + utilizator.getId() + " already exists!");
        }

        return utilizator;
    }

    /**
     * Removes a user from the network by their ID, automatically deleting all friendships
     * in which they were involved using Stream API.
     *
     * @param id the identifier of the user to remove
     * @throws ServiceException if the user is not found or the ID is null
     */
    public void removeUser(Long id) {
        if (id == null) {
            throw new ServiceException("User ID cannot be null!");
        }
        userRepo.findOne(id)
                .orElseThrow(() -> new ServiceException("User with ID " + id + " does not exist!"));

        List<Tuple<Long, Long>> friendshipsToRemove = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(f -> id.equals(f.getIdUser1()) || id.equals(f.getIdUser2()))
                .map(Entity::getId)
                .collect(Collectors.toList());

        friendshipsToRemove.forEach(friendshipRepo::delete);
        userRepo.delete(id);
    }

    /**
     * Creates a friendship between two users and saves it in the friendship repository.
     *
     * @param id1 the identifier of the first user
     * @param id2 the identifier of the second user
     * @throws ServiceException if any user is not found, IDs are identical, or the friendship already exists
     */
    public void addFriendship(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new ServiceException("IDs cannot be null!");
        }
        if (id1.equals(id2)) {
            throw new ServiceException("A user cannot be friends with themselves!");
        }
        userRepo.findOne(id1)
                .orElseThrow(() -> new ServiceException("User with ID " + id1 + " does not exist!"));
        userRepo.findOne(id2)
                .orElseThrow(() -> new ServiceException("User with ID " + id2 + " does not exist!"));

        Friendship friendship = new Friendship(id1, id2);
        Optional<Friendship> existing = friendshipRepo.save(friendship);
        if (existing.isPresent()) {
            throw new ServiceException("Friendship already exists!");
        }
    }

    /**
     * Removes a friendship between two users from the friendship repository.
     *
     * @param id1 the identifier of the first user
     * @param id2 the identifier of the second user
     * @throws ServiceException if any user is not found or the friendship does not exist
     */
    public void removeFriendship(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new ServiceException("IDs cannot be null!");
        }
        userRepo.findOne(id1)
                .orElseThrow(() -> new ServiceException("User with ID " + id1 + " does not exist!"));
        userRepo.findOne(id2)
                .orElseThrow(() -> new ServiceException("User with ID " + id2 + " does not exist!"));

        Tuple<Long, Long> key = new Tuple<>(Math.min(id1, id2), Math.max(id1, id2));
        Optional<Friendship> deleted = friendshipRepo.delete(key);
        if (deleted.isEmpty()) {
            throw new ServiceException("Friendship does not exist!");
        }
    }

    /**
     * Returns the IDs of all friends of a given user using Stream API filter and map.
     *
     * @param userId the identifier of the user
     * @return a list containing the IDs of the user's friends
     */
    public List<Long> getFriendIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(f -> userId.equals(f.getIdUser1()) || userId.equals(f.getIdUser2()))
                .map(f -> userId.equals(f.getIdUser1()) ? f.getIdUser2() : f.getIdUser1())
                .collect(Collectors.toList());
    }

    /**
     * Returns all users who are friends with the specified user using Stream API and method references.
     *
     * @param userId the identifier of the user
     * @return the list of friend users
     * @throws IllegalArgumentException if the ID is null
     * @throws ServiceException         if the user does not exist
     */
    public List<Utilizator> getFriendsOfUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null!");
        }
        userRepo.findOne(userId)
                .orElseThrow(() -> new ServiceException("User with ID " + userId + " does not exist!"));

        return getFriendIds(userId).stream()
                .map(userRepo::findOne)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    /**
     * Filters users in the network according to a given predicate.
     *
     * @param filter the search criteria predicate
     * @return list of matching users
     */
    public List<Utilizator> filterUsers(Predicate<Utilizator> filter) {
        Objects.requireNonNull(filter, "Filter predicate cannot be null");
        return userRepo.findAll(filter);
    }

    /**
     * Generates the next unique identifier in a deterministic sequential manner using Stream API max.
     *
     * @return the new identifier
     */
    private Long generateId() {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .map(Utilizator::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1L;
    }

    /**
     * Retrieves a paginated slice of users from the network.
     *
     * @param pageNumber the 0-based page index to retrieve (must be &gt;= 0)
     * @param pageSize   the maximum number of users per page (must be &gt; 0)
     * @return a {@link Page} containing the users on the requested page and the total user count
     * @throws IllegalArgumentException      if {@code pageNumber < 0} or {@code pageSize <= 0}
     * @throws UnsupportedOperationException if the underlying user repository does not support pagination
     */
    public Page<Utilizator> getUsersOnPage(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0.");
        }
        if (!(userRepo instanceof PagingRepository<Long, Utilizator> pagingRepo)) {
            throw new UnsupportedOperationException("Pagination is not supported by this repository.");
        }
        Pageable pageable = new Pageable(pageNumber, pageSize);
        return pagingRepo.findAllOnPage(pageable);
    }

    /**
     * Retrieves a paginated slice of friendships from the network.
     *
     * @param pageNumber the 0-based page index to retrieve (must be &gt;= 0)
     * @param pageSize   the maximum number of friendships per page (must be &gt; 0)
     * @return a {@link Page} containing the friendships on the requested page and the total friendship count
     * @throws IllegalArgumentException      if {@code pageNumber < 0} or {@code pageSize <= 0}
     * @throws UnsupportedOperationException if the underlying friendship repository does not support pagination
     */
    public Page<Friendship> getFriendshipsOnPage(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0.");
        }
        if (!(friendshipRepo instanceof PagingRepository<?, ?>)) {
            throw new UnsupportedOperationException("Pagination is not supported by this friendship repository.");
        }

        PagingRepository<?, Friendship> pagingRepo = (PagingRepository<?, Friendship>) friendshipRepo;

        Pageable pageable = new Pageable(pageNumber, pageSize);
        return pagingRepo.findAllOnPage(pageable);
    }

    /**
     * Retrieves a paginated slice of friends for a given user from the network.
     *
     * @param userId     the identifier of the user whose friends are being queried
     * @param pageNumber the 0-based page index to retrieve (must be &gt;= 0)
     * @param pageSize   the maximum number of friends per page (must be &gt; 0)
     * @return a {@link Page} containing the user's friends on the requested page and the total friend count
     * @throws IllegalArgumentException      if {@code userId == null}, {@code pageNumber < 0}, or {@code pageSize <= 0}
     * @throws ServiceException              if the user does not exist
     * @throws UnsupportedOperationException if the underlying friendship repository does not support friends pagination
     */
    public Page<Utilizator> getFriendsOnPage(Long userId, int pageNumber, int pageSize) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null!");
        }
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0.");
        }
        userRepo.findOne(userId)
                .orElseThrow(() -> new ServiceException("User with ID " + userId + " does not exist!"));

        if (friendshipRepo instanceof FriendshipDbRepository dbRepo) {
            Pageable pageable = new Pageable(pageNumber, pageSize);
            return dbRepo.findFriendsOnPage(userId, pageable);
        }

        throw new UnsupportedOperationException("Paging friends is only supported by database repository.");
    }

    /**
     * Returns all users in the network.
     *
     * @return an iterable collection of all users
     */
    public Iterable<Utilizator> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Returns all friendships in the network.
     *
     * @return an iterable collection of all friendships
     */
    public Iterable<Friendship> getAllFriendships() {
        return friendshipRepo.findAll();
    }

    /**
     * Returns the user repository.
     *
     * @return the user repository
     */
    public Repository<Long, Utilizator> getUserRepo() {
        return userRepo;
    }

    /**
     * Returns the friendship repository.
     *
     * @return the friendship repository
     */
    public Repository<Tuple<Long, Long>, Friendship> getFriendshipRepo() {
        return friendshipRepo;
    }
}
