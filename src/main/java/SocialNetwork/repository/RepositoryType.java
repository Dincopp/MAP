package SocialNetwork.repository;

/**
 * Enumeration representing the supported repository storage types.
 */
public enum RepositoryType {
    /**
     * In-memory storage.
     */
    MEMORY,

    /**
     * File-based storage (TXT).
     */
    FILE,

    /**
     * Relational database storage.
     */
    DATABASE;
}
