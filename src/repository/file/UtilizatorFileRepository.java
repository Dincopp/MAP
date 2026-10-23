package repository.file;

import domain.Utilizator;
import Validation.Validator;
import repository.RepositoryException;

/**
 * Repository for persisting users into a text file (CSV).
 */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    /**
     * Constructs a file-based user repository.
     *
     * @param validator the validator used to validate entities
     * @param fileName  the path of the CSV file
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * Extracts a {@link Utilizator} entity from a text line.
     *
     * @param line the text line from the file
     * @return the parsed user entity
     * @throws RepositoryException if the ID on the line is invalid
     */
    @Override
    public Utilizator extractEntity(String line) {
        String[] parts = line.split(";");
        try {
            Long id = Long.parseLong(parts[0].trim());
            String firstName = parts[1].trim();
            String lastName = parts[2].trim();
            Utilizator u = new Utilizator(firstName, lastName);
            u.setId(id);
            return u;
        } catch (NumberFormatException e) {
            throw new RepositoryException("Invalid ID on line!", e);
        }
    }

    /**
     * Formats a {@link Utilizator} entity into a text line for file persistence.
     *
     * @param entity the user entity to convert
     * @return the formatted text line
     */
    @Override
    public String createEntityasString(Utilizator entity) {
        return entity.getId() + ";" + entity.getFirstName() + ";" + entity.getLastName();
    }
}
