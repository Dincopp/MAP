package service;

import domain.Friendship;
import Validation.FriendshipValidator;
import domain.Tuple;
import domain.Utilizator;
import repository.memory.FriendshipRepository;
import repository.Repository;

import java.util.*;

/**
 * Service responsible for managing social network business logic:
 * adding/removing users, establishing/removing friendships,
 * and retrieving network-related statistics.
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
     *
     * @param utilizator the user to add
     * @return the added user
     * @throws ServiceException if the user is null, a user with the same first name exists, or a user with the same ID already exists
     */
    public Utilizator addUser(Utilizator utilizator) {
        if (utilizator == null) {
            throw new ServiceException("User cannot be null!");
        }
        for (Utilizator u : userRepo.findAll()) {
            if (utilizator.getFirstName().equals(u.getFirstName())) {
                throw new ServiceException("A user with this first name already exists!");
            }
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
     * in which they were involved.
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

        List<Tuple<Long, Long>> friendshipsToRemove = new ArrayList<>();
        for (Friendship f : friendshipRepo.findAll()) {
            if (id.equals(f.getIdUser1()) || id.equals(f.getIdUser2())) {
                friendshipsToRemove.add(f.getId());
            }
        }
        for (Tuple<Long, Long> key : friendshipsToRemove) {
            friendshipRepo.delete(key);
        }

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
     * Returns the IDs of all friends of a given user.
     *
     * @param userId the identifier of the user
     * @return a list containing the IDs of the user's friends
     */
    public List<Long> getFriendIds(Long userId) {
        List<Long> friends = new ArrayList<>();
        if (userId == null) {
            return friends;
        }
        for (Friendship f : friendshipRepo.findAll()) {
            if (userId.equals(f.getIdUser1())) {
                friends.add(f.getIdUser2());
            } else if (userId.equals(f.getIdUser2())) {
                friends.add(f.getIdUser1());
            }
        }
        return friends;
    }

    /**
     * Returns all users who are friends with the specified user.
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
        List<Utilizator> friends = new ArrayList<>();
        for (Long friendId : getFriendIds(userId)) {
            userRepo.findOne(friendId).ifPresent(friends::add);
        }
        return friends;
    }

    /**
     * Generates the next unique identifier in a deterministic sequential manner.
     *
     * @return the new identifier
     */
    private Long generateId() {
        long maxId = 0L;
        for (Utilizator u : userRepo.findAll()) {
            if (u.getId() != null && u.getId() > maxId) {
                maxId = u.getId();
            }
        }
        return maxId + 1;
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
