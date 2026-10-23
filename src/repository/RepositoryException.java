package repository;

/**
 * Exception thrown when a repository-level operation fails.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Constructs a new repository exception with the specified detail message.
     *
     * @param message the detail message
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new repository exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
