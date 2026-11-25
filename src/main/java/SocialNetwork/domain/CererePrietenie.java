package SocialNetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity modeling a friend request between two users in the social network.
 * Stores the sender user ID, recipient user ID, current {@link FriendRequestStatus},
 * and the timestamp when the request was created.
 */
public class CererePrietenie extends Entity<Long> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long from;
    private Long to;
    private FriendRequestStatus status;
    private LocalDateTime date;

    /**
     * Constructs a new pending friend request between two users at the current date and time.
     *
     * @param from the ID of the user sending the request
     * @param to   the ID of the recipient user
     */
    public CererePrietenie(Long from, Long to) {
        this(from, to, FriendRequestStatus.PENDING, LocalDateTime.now());
    }

    /**
     * Constructs a friend request with explicit status and timestamp.
     *
     * @param from   the ID of the user sending the request
     * @param to     the ID of the recipient user
     * @param status the status of the friend request
     * @param date   the timestamp when the request was created
     */
    public CererePrietenie(Long from, Long to, FriendRequestStatus status, LocalDateTime date) {
        this.from = from;
        this.to = to;
        this.status = status != null ? status : FriendRequestStatus.PENDING;
        this.date = date != null ? date : LocalDateTime.now();
    }

    /**
     * Returns the ID of the user who sent the friend request.
     *
     * @return the sender user ID
     */
    public Long getFrom() {
        return from;
    }

    /**
     * Sets the ID of the user who sent the friend request.
     *
     * @param from the sender user ID
     */
    public void setFrom(Long from) {
        this.from = from;
    }

    /**
     * Returns the ID of the recipient user.
     *
     * @return the recipient user ID
     */
    public Long getTo() {
        return to;
    }

    /**
     * Sets the ID of the recipient user.
     *
     * @param to the recipient user ID
     */
    public void setTo(Long to) {
        this.to = to;
    }

    /**
     * Returns the current status of the friend request.
     *
     * @return the {@link FriendRequestStatus}
     */
    public FriendRequestStatus getStatus() {
        return status;
    }

    /**
     * Updates the status of the friend request.
     *
     * @param status the new {@link FriendRequestStatus}
     */
    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    /**
     * Returns the timestamp when the friend request was created.
     *
     * @return the creation date and time
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the timestamp of the friend request.
     *
     * @param date the creation date and time
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns a formatted representation of the request date.
     *
     * @return formatted date string, or empty string if date is null
     */
    public String getFormattedDate() {
        return date != null ? date.format(FORMATTER) : "";
    }

    /**
     * Returns the string representation of the friend request.
     *
     * @return string detailing the friend request attributes
     */
    @Override
    public String toString() {
        return "CererePrietenie{" +
                "id=" + getId() +
                ", from=" + from +
                ", to=" + to +
                ", status=" + status +
                ", date=" + getFormattedDate() +
                '}';
    }
}
