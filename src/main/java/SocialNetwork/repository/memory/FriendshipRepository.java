package SocialNetwork.repository.memory;

import SocialNetwork.domain.Entity;
import SocialNetwork.domain.Friendship;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.domain.Tuple;
import SocialNetwork.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Repository for managing friendship relationships in memory using Java 8 Stream API.
 */
public class FriendshipRepository extends InMemoryRepository<Tuple<Long, Long>, Friendship> {

    /**
     * Constructs a repository based on a friendship validator.
     *
     * @param validator the validator used to verify entities
     */
    public FriendshipRepository(Validator<Friendship> validator) {
        super(validator);
    }

    /**
     * Constructs a repository using the default validator {@link FriendshipValidator}.
     */
    public FriendshipRepository() {
        super(new FriendshipValidator());
    }

    /**
     * Returns all friendships associated with a specific user using Stream API.
     *
     * @param userId the identifier of the user
     * @return a list of all friendships involving the user
     */
    public List<Friendship> getFriendshipsOfUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(f -> userId.equals(f.getIdUser1()) || userId.equals(f.getIdUser2()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a friendship relationship exists between two users.
     *
     * @param id1 the ID of the first user
     * @param id2 the ID of the second user
     * @return {@code true} if they are friends, {@code false} otherwise
     */
    public boolean areFriends(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            return false;
        }
        Tuple<Long, Long> key = new Tuple<>(Math.min(id1, id2), Math.max(id1, id2));
        return findOne(key).isPresent();
    }

    /**
     * Deletes all friendships involving a specific user using Stream API and method references.
     *
     * @param userId the identifier of the user
     * @return the number of friendships deleted
     */
    public int removeFriendshipsOfUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<Tuple<Long, Long>> keysToRemove = StreamSupport.stream(findAll().spliterator(), false)
                .filter(f -> userId.equals(f.getIdUser1()) || userId.equals(f.getIdUser2()))
                .map(Entity::getId)
                .collect(Collectors.toList());

        keysToRemove.forEach(this::delete);
        return keysToRemove.size();
    }
}
