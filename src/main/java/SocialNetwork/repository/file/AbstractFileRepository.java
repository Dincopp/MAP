package SocialNetwork.repository.file;

import SocialNetwork.repository.RepositoryException;
import SocialNetwork.validation.ValidationException;
import SocialNetwork.domain.Entity;
import SocialNetwork.validation.Validator;
import SocialNetwork.repository.memory.InMemoryRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract base class for persisting entities to a text file using Java 8 Stream API and NIO.2.
 * Extends {@link InMemoryRepository} by adding loading from file via {@link Files#lines(Path)}
 * and automatic saving upon modifications via {@link Files#write(Path, Iterable, java.nio.file.OpenOption...)}.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of the managed entity, extending {@link Entity}
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final String fileName;

    /**
     * Constructs a file-based repository and loads existing data from the file.
     *
     * @param validator the validator used to validate entities
     * @param fileName  the name or path of the file on disk
     */
    public AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        this.fileName = fileName;
        loadData();
    }

    /**
     * Constructs an entity from a line read from the file.
     *
     * @param line the text line from the file
     * @return the reconstituted entity
     */
    public abstract E extractEntity(String line);

    /**
     * Converts an entity into a line of text to be saved in the file.
     *
     * @param entity the entity to convert
     * @return the string representation of the entity
     */
    public abstract String createEntityasString(E entity);

    /**
     * Loads entities from the file using {@link Files#lines(Path)} in a try-with-resources block
     * and processes line indices using {@link IntStream#range(int, int)}.
     */
    private void loadData() {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return;
        }

        List<String> lines;
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            lines = stream.collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        IntStream.range(0, lines.size())
                .forEach(index -> {
                    String line = lines.get(index);
                    int lineNumber = index + 1;
                    if (!line.trim().isEmpty()) {
                        try {
                            E entity = extractEntity(line);
                            super.save(entity);
                        } catch (ValidationException | RepositoryException | NumberFormatException | IndexOutOfBoundsException e) {
                            System.err.println("Line " + lineNumber + " is corrupt and was ignored: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * Writes all entities from memory into the file using Stream API mapping and {@link Files#write(Path, Iterable, java.nio.file.OpenOption...)}.
     *
     * @throws RepositoryException if an I/O error occurs while writing to the file
     */
    protected void writeToFile() {
        Path path = Paths.get(fileName);
        List<String> lines = StreamSupport.stream(findAll().spliterator(), false)
                .map(this::createEntityasString)
                .collect(Collectors.toList());
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Error writing file: " + e.getMessage(), e);
        }
    }

    /**
     * Saves an entity in memory and updates the file on disk if the save succeeded.
     *
     * @param entity the entity to save
     * @return {@link Optional#empty()} if the entity was successfully saved,
     *         or an {@link Optional} containing the entity if it already exists
     */
    @Override
    public Optional<E> save(E entity) {
        Optional<E> res = super.save(entity);
        if (res.isEmpty()) {
            writeToFile();
        }
        return res;
    }

    /**
     * Updates an entity in memory and persists changes to the file on disk if the update succeeded.
     *
     * @param entity the entity with updated fields
     * @return {@link Optional#empty()} if updated successfully, or an {@link Optional} containing the entity if not found
     */
    @Override
    public Optional<E> update(E entity) {
        Optional<E> res = super.update(entity);
        if (res.isEmpty()) {
            writeToFile();
        }
        return res;
    }

    /**
     * Deletes an entity from memory and updates the file on disk if the operation succeeded.
     *
     * @param id the identifier of the entity to delete
     * @return an {@link Optional} containing the deleted entity, or {@link Optional#empty()} if not found
     */
    @Override
    public Optional<E> delete(ID id) {
        Optional<E> res = super.delete(id);
        if (res.isPresent()) {
            writeToFile();
        }
        return res;
    }
}
