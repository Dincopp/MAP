package SocialNetwork.service;

import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.domain.FriendRequestStatus;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.database.CererePrietenieDbRepository;
import SocialNetwork.utils.events.ChangeEventType;
import SocialNetwork.utils.events.EntityChangeEvent;
import SocialNetwork.utils.observer.Observable;
import SocialNetwork.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service responsible for managing {@link CererePrietenie} (friend request) workflows
 * and enforcing domain rules:
 * <ul>
 *     <li>A user cannot send a friend request to themselves.</li>
 *     <li>A user cannot send a friend request to an existing friend.</li>
 *     <li>No duplicate {@code PENDING} friend request may exist between two users in either direction.</li>
 *     <li>Only the recipient can respond to a friend request, and only once while it is {@code PENDING}.</li>
 *     <li>Approving a friend request persists the corresponding friendship via {@link NetworkService}.</li>
 * </ul>
 * Implements {@link Observable} to notify UI controllers upon request creation or status updates.
 */
public class CererePrietenieService implements Observable<EntityChangeEvent<CererePrietenie>> {
    private final Repository<Long, CererePrietenie> requestRepo;
    private final Repository<Long, Utilizator> userRepo;
    private final NetworkService networkService;
    private final List<Observer<EntityChangeEvent<CererePrietenie>>> observers = new CopyOnWriteArrayList<>();

    /**
     * Constructs a {@code CererePrietenieService} with the required repositories and network service.
     *
     * @param requestRepo    the repository for persisting friend requests
     * @param userRepo       the repository used to verify user existence
     * @param networkService the network service used to check and create friendships
     */
    public CererePrietenieService(Repository<Long, CererePrietenie> requestRepo,
                                  Repository<Long, Utilizator> userRepo,
                                  NetworkService networkService) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.networkService = networkService;
    }

    /**
     * Registers an observer to be notified when friend requests are created or updated.
     *
     * @param observer the observer to register
     */
    @Override
    public void addObserver(Observer<EntityChangeEvent<CererePrietenie>> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Unregisters a previously registered observer.
     *
     * @param observer the observer to remove
     */
    @Override
    public void removeObserver(Observer<EntityChangeEvent<CererePrietenie>> observer) {
        observers.remove(observer);
    }

    /**
     * Broadcasts a friend request change event to all registered observers.
     *
     * @param event the change event to broadcast
     */
    @Override
    public void notifyObservers(EntityChangeEvent<CererePrietenie> event) {
        observers.forEach(observer -> observer.update(event));
    }

    /**
     * Sends a new friend request from {@code fromId} to {@code toId} after validating all business rules.
     *
     * @param fromId the ID of the user sending the friend request
     * @param toId   the ID of the user receiving the friend request
     * @return the newly created {@link CererePrietenie}
     * @throws IllegalArgumentException if IDs are null, identical, users do not exist,
     *                                  users are already friends, or a pending request already exists in either direction
     */
    public CererePrietenie sendRequest(Long fromId, Long toId) {
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("Sender and recipient IDs cannot be null!");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Nu puteți trimite o cerere de prietenie către propriul cont!");
        }

        userRepo.findOne(fromId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul expeditor (ID " + fromId + ") nu există!"));
        userRepo.findOne(toId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul destinatar (ID " + toId + ") nu există!"));

        if (networkService.areFriends(fromId, toId)) {
            throw new IllegalArgumentException("Utilizatorii sunt deja prieteni!");
        }

        boolean pendingExists = StreamSupport.stream(requestRepo.findAll().spliterator(), false)
                .anyMatch(r -> r.getStatus() == FriendRequestStatus.PENDING &&
                        ((fromId.equals(r.getFrom()) && toId.equals(r.getTo())) ||
                         (toId.equals(r.getFrom()) && fromId.equals(r.getTo()))));

        if (pendingExists) {
            throw new IllegalArgumentException("Există deja o cerere de prietenie PENDING între acești utilizatori!");
        }

        CererePrietenie request = new CererePrietenie(fromId, toId, FriendRequestStatus.PENDING, LocalDateTime.now());
        if (!(requestRepo instanceof CererePrietenieDbRepository) && request.getId() == null) {
            request.setId(generateId());
        }

        requestRepo.save(request);
        notifyObservers(new EntityChangeEvent<>(ChangeEventType.ADD, request));
        return request;
    }

    /**
     * Responds to an existing {@code PENDING} friend request by setting its status to
     * {@link FriendRequestStatus#APPROVED} or {@link FriendRequestStatus#REJECTED}.
     * Only the recipient may respond, and only once. If approved, the friendship is saved.
     *
     * @param requestId   the identifier of the friend request
     * @param responderId the identifier of the user responding to the request (must match {@code to})
     * @param newStatus   the decision status ({@code APPROVED} or {@code REJECTED})
     * @return the updated {@link CererePrietenie}
     * @throws IllegalArgumentException if arguments are invalid, request is not found, responder is not the recipient,
     *                                  or the request has already been responded to
     */
    public CererePrietenie respondToRequest(Long requestId, Long responderId, FriendRequestStatus newStatus) {
        if (requestId == null || responderId == null || newStatus == null) {
            throw new IllegalArgumentException("Request ID, responder ID, and status cannot be null!");
        }
        if (newStatus != FriendRequestStatus.APPROVED && newStatus != FriendRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Răspuns invalid! Statusul trebuie să fie APPROVED sau REJECTED.");
        }

        CererePrietenie request = requestRepo.findOne(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Cererea de prietenie cu ID " + requestId + " nu există!"));

        if (!responderId.equals(request.getTo())) {
            throw new IllegalArgumentException("Doar destinatarul poate răspunde la această cerere de prietenie!");
        }
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("S-a răspuns deja la această cerere de prietenie (status: " + request.getStatus() + ")!");
        }

        CererePrietenie oldCopy = new CererePrietenie(request.getFrom(), request.getTo(), request.getStatus(), request.getDate());
        oldCopy.setId(request.getId());

        request.setStatus(newStatus);
        requestRepo.update(request);

        if (newStatus == FriendRequestStatus.APPROVED) {
            networkService.addFriendship(request.getFrom(), request.getTo());
        }

        notifyObservers(new EntityChangeEvent<>(ChangeEventType.UPDATE, request, oldCopy));
        return request;
    }

    /**
     * Approves a pending friend request on behalf of the recipient and creates the friendship.
     *
     * @param requestId   the identifier of the friend request
     * @param responderId the identifier of the recipient user
     * @return the updated {@link CererePrietenie}
     */
    public CererePrietenie acceptRequest(Long requestId, Long responderId) {
        return respondToRequest(requestId, responderId, FriendRequestStatus.APPROVED);
    }

    /**
     * Rejects a pending friend request on behalf of the recipient.
     *
     * @param requestId   the identifier of the friend request
     * @param responderId the identifier of the recipient user
     * @return the updated {@link CererePrietenie}
     */
    public CererePrietenie rejectRequest(Long requestId, Long responderId) {
        return respondToRequest(requestId, responderId, FriendRequestStatus.REJECTED);
    }

    /**
     * Returns all friend requests received by the specified user, ordered with {@code PENDING} requests first
     * and then by date descending.
     *
     * @param userId the recipient user ID
     * @return a list of received {@link CererePrietenie} entities
     * @throws IllegalArgumentException if {@code userId} is {@code null}
     */
    public List<CererePrietenie> getReceivedRequests(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null!");
        }
        return StreamSupport.stream(requestRepo.findAll().spliterator(), false)
                .filter(r -> userId.equals(r.getTo()))
                .sorted(Comparator
                        .comparing((CererePrietenie r) -> r.getStatus() != FriendRequestStatus.PENDING)
                        .thenComparing(CererePrietenie::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all friend requests in the system.
     *
     * @return an iterable over all {@link CererePrietenie} entities
     */
    public Iterable<CererePrietenie> getAllRequests() {
        return requestRepo.findAll();
    }

    /**
     * Generates the next unique identifier when backed by an in-memory repository.
     *
     * @return the generated ID
     */
    private Long generateId() {
        return StreamSupport.stream(requestRepo.findAll().spliterator(), false)
                .map(CererePrietenie::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1L;
    }
}
