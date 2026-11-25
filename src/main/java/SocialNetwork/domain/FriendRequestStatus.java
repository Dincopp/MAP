package SocialNetwork.domain;

/**
 * Enumeration representing the possible states of a {@link CererePrietenie} (friend request).
 */
public enum FriendRequestStatus {
    /**
     * The friend request has been sent and is awaiting a response from the recipient.
     */
    PENDING,

    /**
     * The friend request has been accepted by the recipient.
     */
    APPROVED,

    /**
     * The friend request has been declined by the recipient.
     */
    REJECTED
}
