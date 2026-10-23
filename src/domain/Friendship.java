package domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity modeling a friendship relationship between two users.
 * The friendship identifier is a {@link Tuple} consisting of the two user IDs,
 * normalized such that the relationship (u1, u2) is equivalent to (u2, u1).
 */
public class Friendship extends Entity<Tuple<Long, Long>> {
    private LocalDateTime date;

    /**
     * Constructs a friendship relationship between two users at the current date and time.
     *
     * @param idUser1 the ID of the first user
     * @param idUser2 the ID of the second user
     */
    public Friendship(Long idUser1, Long idUser2) {
        this(idUser1, idUser2, LocalDateTime.now());
    }

    /**
     * Constructs a friendship relationship between two users at a specified date and time.
     *
     * @param idUser1 the ID of the first user
     * @param idUser2 the ID of the second user
     * @param date    the date and time when the friendship was created
     */
    public Friendship(Long idUser1, Long idUser2, LocalDateTime date) {
        if (idUser1 != null && idUser2 != null) {
            Long min = Math.min(idUser1, idUser2);
            Long max = Math.max(idUser1, idUser2);
            setId(new Tuple<>(min, max));
        } else {
            setId(new Tuple<>(idUser1, idUser2));
        }
        this.date = (date != null) ? date : LocalDateTime.now();
    }

    /**
     * Returns the ID of the first user (the one with the smaller value after normalization).
     *
     * @return the ID of the first user
     */
    public Long getIdUser1() {
        return getId() != null ? getId().getLeft() : null;
    }

    /**
     * Returns the ID of the second user (the one with the larger value after normalization).
     *
     * @return the ID of the second user
     */
    public Long getIdUser2() {
        return getId() != null ? getId().getRight() : null;
    }

    /**
     * Returns the date and time when the friendship was established.
     *
     * @return the date of the friendship
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the date and time when the friendship was established.
     *
     * @param date the new date
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the string representation of the friendship.
     *
     * @return the string representing the friendship
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Friendship{" +
                "idUser1=" + getIdUser1() +
                ", idUser2=" + getIdUser2() +
                ", date=" + (date != null ? date.format(formatter) : "null") +
                '}';
    }
}
