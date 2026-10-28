package SocialNetwork.validation;

import SocialNetwork.domain.Friendship;

/**
 * Validator for the {@link Friendship} entity.
 * Verifies the integrity of a friendship relationship (non-null, strictly positive, and distinct IDs).
 */
public class FriendshipValidator implements Validator<Friendship> {

    /**
     * Validates a {@link Friendship} entity.
     *
     * @param entity the friendship entity to validate
     * @throws ValidationException if the friendship is null, IDs are null, user IDs are identical, or user IDs are not positive numbers
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if (entity == null) {
            throw new ValidationException("Friendship cannot be null!");
        }
        if (entity.getId() == null || entity.getIdUser1() == null || entity.getIdUser2() == null) {
            throw new ValidationException("User IDs cannot be null!");
        }
        if (entity.getIdUser1().equals(entity.getIdUser2())) {
            throw new ValidationException("A user cannot be friends with themselves!");
        }
        if (entity.getIdUser1() <= 0 || entity.getIdUser2() <= 0) {
            throw new ValidationException("User IDs must be positive numbers!");
        }
    }
}
