package repository.file;

import domain.Friendship;
import domain.Tuple;
import Validation.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Repository for persisting friendship relationships to a text file (CSV).
 */
public class FriendshipFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Friendship> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Constructs a file-based friendship repository.
     *
     * @param validator the validator used to verify entities
     * @param fileName  the path of the CSV file
     */
    public FriendshipFileRepository(Validator<Friendship> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * Extracts a {@link Friendship} entity from a text line.
     *
     * @param line the text line from the file
     * @return the parsed friendship entity
     */
    @Override
    public Friendship extractEntity(String line) {
        String[] parts = line.split(";");
        Long id1 = Long.parseLong(parts[0].trim());
        Long id2 = Long.parseLong(parts[1].trim());
        LocalDateTime date = (parts.length > 2 && !parts[2].trim().isEmpty())
                ? LocalDateTime.parse(parts[2].trim(), FORMATTER)
                : LocalDateTime.now();
        return new Friendship(id1, id2, date);
    }

    /**
     * Formats a {@link Friendship} entity into a text line for file persistence.
     *
     * @param entity the friendship entity to convert
     * @return the formatted text line
     */
    @Override
    public String createEntityasString(Friendship entity) {
        return entity.getIdUser1() + ";" + entity.getIdUser2() + ";" + entity.getDate().format(FORMATTER);
    }
}
