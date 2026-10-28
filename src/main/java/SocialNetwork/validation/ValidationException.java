package SocialNetwork.validation;

/**
 * Exception thrown when validation of an entity fails.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new validation exception with no detail message.
     */
    public ValidationException() {
    }

    /**
     * Constructs a new validation exception with a detailed message.
     *
     * @param message the descriptive error message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with a message and cause.
     *
     * @param message the descriptive error message
     * @param cause   the cause that triggered this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new validation exception with a cause.
     *
     * @param cause the cause that triggered this exception
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new validation exception with full details on suppression and stack trace.
     *
     * @param message            the descriptive error message
     * @param cause              the cause that triggered this exception
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
