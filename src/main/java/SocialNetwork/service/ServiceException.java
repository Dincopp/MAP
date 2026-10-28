package SocialNetwork.service;

/**
 * Exception thrown when a business or service-level logic error occurs.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructs a new service exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }
}
