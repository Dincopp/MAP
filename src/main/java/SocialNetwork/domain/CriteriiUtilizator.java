package SocialNetwork.domain;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class providing static filtering criteria for {@link Utilizator} entities.
 * Can be used with {@link SocialNetwork.repository.Repository#findAll(Predicate)}
 * and combined using {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)},
 * {@link Predicate#negate()}, or {@link Predicate#not(Predicate)}.
 */
public final class CriteriiUtilizator {

    private CriteriiUtilizator() {

    }

    /**
     * Checks if a user has no friends.
     * Can be passed directly as a method reference {@code CriteriiUtilizator::faraPrieteni}.
     *
     * @param u the user to check; cannot be {@code null}
     * @return {@code true} if the user has no friends, {@code false} otherwise
     * @throws NullPointerException if {@code u} is {@code null}
     */
    public static boolean faraPrieteni(Utilizator u) {
        Objects.requireNonNull(u, "User cannot be null");
        return u.getFriends() == null || u.getFriends().isEmpty();
    }

    /**
     * Returns a predicate matching users with the specified last name (case-insensitive).
     *
     * @param lastName the last name to match; cannot be {@code null}
     * @return a {@link Predicate} testing if a user has the given last name
     * @throws NullPointerException if {@code lastName} is {@code null}
     */
    public static Predicate<Utilizator> hasLastName(String lastName) {
        Objects.requireNonNull(lastName, "Last name cannot be null");
        return u -> u.getLastName() != null && u.getLastName().equalsIgnoreCase(lastName);
    }

    /**
     * Returns a predicate matching users with the specified first name (case-insensitive).
     *
     * @param firstName the first name to match; cannot be {@code null}
     * @return a {@link Predicate} testing if a user has the given first name
     * @throws NullPointerException if {@code firstName} is {@code null}
     */
    public static Predicate<Utilizator> hasFirstName(String firstName) {
        Objects.requireNonNull(firstName, "First name cannot be null");
        return u -> u.getFirstName() != null && u.getFirstName().equalsIgnoreCase(firstName);
    }

    /**
     * Returns a predicate matching users whose first or last name starts with the given prefix (case-insensitive).
     *
     * @param prefix the prefix to look for; cannot be {@code null}
     * @return a {@link Predicate} testing if a user's name starts with {@code prefix}
     * @throws NullPointerException if {@code prefix} is {@code null}
     */
    public static Predicate<Utilizator> nameStartsWith(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        return u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().startsWith(prefix.toLowerCase()))
                || (u.getLastName() != null && u.getLastName().toLowerCase().startsWith(prefix.toLowerCase()));
    }

    /**
     * Returns a predicate matching users whose identifier is strictly greater than the given value.
     *
     * @param minId the threshold identifier; cannot be {@code null}
     * @return a {@link Predicate} testing if a user's ID is greater than {@code minId}
     * @throws NullPointerException if {@code minId} is {@code null}
     */
    public static Predicate<Utilizator> hasIdGreaterThan(Long minId) {
        Objects.requireNonNull(minId, "minId cannot be null");
        return u -> u.getId() != null && u.getId() > minId;
    }
}
