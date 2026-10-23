package repository.file;

import Validation.ValidationException;
import domain.Entity;
import Validation.Validator;
import repository.memory.InMemoryRepository;
import repository.RepositoryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Abstract base class for persisting entities to a text file.
 * Extends {@link InMemoryRepository} by adding loading from file and automatic saving upon modifications.
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

    private void loadData() {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    try {
                        E entity = extractEntity(line);
                        super.save(entity);
                    } catch (ValidationException | NumberFormatException | IndexOutOfBoundsException e) {
                        System.err.println("Line " + lineNumber + " is corrupt and was ignored: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Writes all entities from memory into the file.
     */
    protected void writeToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            for (E entity : findAll()) {
                bw.write(createEntityasString(entity));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
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
