package SocialNetwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity modeling a user of the social network.
 */
public class Utilizator extends Entity<Long> {
    private String firstName;
    private String lastName;
    private final List<Utilizator> friends = new ArrayList<>();

    /**
     * Constructs a user based on first name and last name.
     *
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     */
    public Utilizator(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the list of friends of this user.
     *
     * @return the list of friends
     */
    public List<Utilizator> getFriends() {
        return friends;
    }

    /**
     * Adds a friend to this user's list of friends if not already present.
     *
     * @param friend the friend user to add
     */
    public void addFriend(Utilizator friend) {
        if (friend != null && !friends.contains(friend)) {
            friends.add(friend);
        }
    }

    /**
     * Removes a friend from this user's list of friends.
     *
     * @param friend the friend user to remove
     */
    public void removeFriend(Utilizator friend) {
        friends.remove(friend);
    }

    /**
     * Checks equality between two users.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator that)) return false;
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    /**
     * Computes the hash code for the user.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        }
        return Objects.hash(firstName, lastName);
    }

    /**
     * Returns the string representation of the user.
     *
     * @return the string representation of the Utilizator object
     */
    @Override
    public String toString() {
        return "Utilizator{" + "id=" + getId() + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + '}';
    }
}
