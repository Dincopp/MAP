package SocialNetwork.validation;

import SocialNetwork.domain.CererePrietenie;

/**
 * Validator for the {@link CererePrietenie} entity.
 * Ensures that sender, recipient, status, and date are valid,
 * user IDs are strictly positive, and a user does not send a request to themselves.
 */
public class CererePrietenieValidator implements Validator<CererePrietenie> {

    /**
     * Validates a {@link CererePrietenie} instance.
     *
     * @param entity the friend request to validate
     * @throws ValidationException if any domain validation constraint is violated
     */
    @Override
    public void validate(CererePrietenie entity) throws ValidationException {
        if (entity == null) {
            throw new ValidationException("Friend request cannot be null!");
        }
        if (entity.getFrom() == null || entity.getTo() == null) {
            throw new ValidationException("Sender and recipient user IDs cannot be null!");
        }
        if (entity.getFrom() <= 0 || entity.getTo() <= 0) {
            throw new ValidationException("User IDs must be positive numbers!");
        }
        if (entity.getFrom().equals(entity.getTo())) {
            throw new ValidationException("A user cannot send a friend request to themselves!");
        }
        if (entity.getStatus() == null) {
            throw new ValidationException("Friend request status cannot be null!");
        }
        if (entity.getDate() == null) {
            throw new ValidationException("Friend request date cannot be null!");
        }
    }
}
