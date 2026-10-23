package repository.memory;

import domain.Friendship;
import Validation.FriendshipValidator;
import domain.Tuple;
import Validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing friendship relationships in memory.
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
     * Returns all friendships associated with a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of all friendships involving the user
     */
    public List<Friendship> getFriendshipsOfUser(Long userId) {
        List<Friendship> result = new ArrayList<>();
        if (userId == null) {
            return result;
        }
        for (Friendship f : findAll()) {
            if (userId.equals(f.getIdUser1()) || userId.equals(f.getIdUser2())) {
                result.add(f);
            }
        }
        return result;
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
     * Deletes all friendships involving a specific user.
     *
     * @param userId the identifier of the user
     * @return the number of friendships deleted
     */
    public int removeFriendshipsOfUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<Tuple<Long, Long>> keysToRemove = new ArrayList<>();
        for (Friendship f : findAll()) {
            if (userId.equals(f.getIdUser1()) || userId.equals(f.getIdUser2())) {
                keysToRemove.add(f.getId());
            }
        }
        for (Tuple<Long, Long> key : keysToRemove) {
            delete(key);
        }
        return keysToRemove.size();
    }
}
