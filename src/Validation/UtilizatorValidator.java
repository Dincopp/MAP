package Validation;

import domain.Utilizator;

/**
 * Specific validator for the {@link Utilizator} entity.
 * Ensures that the first name and last name of the user are not null or empty.
 */
public class UtilizatorValidator implements Validator<Utilizator> {
    /**
     * Validates a {@link Utilizator} entity.
     *
     * @param entity the user to validate
     * @throws ValidationException if the user is null or if the first name or last name is null or empty
     */
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        if (entity == null) {
            throw new ValidationException("User cannot be null!");
        }
        if (entity.getFirstName() == null || entity.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name cannot be empty!");
        }
        if (entity.getLastName() == null || entity.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name cannot be empty!");
        }
    }
}
