package SocialNetwork;

import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.domain.FriendRequestStatus;
import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.memory.FriendshipRepository;
import SocialNetwork.repository.memory.InMemoryRepository;
import SocialNetwork.service.CererePrietenieService;
import SocialNetwork.service.NetworkService;
import SocialNetwork.validation.CererePrietenieValidator;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.validation.UtilizatorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FriendRequestTest {
    private Repository<Long, Utilizator> userRepo;
    private Repository<Tuple<Long, Long>, Friendship> friendshipRepo;
    private Repository<Long, CererePrietenie> requestRepo;
    private NetworkService networkService;
    private CererePrietenieService cerereService;

    private Utilizator u1;
    private Utilizator u2;
    private Utilizator u3;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(new UtilizatorValidator());
        friendshipRepo = new FriendshipRepository(new FriendshipValidator());
        requestRepo = new InMemoryRepository<>(new CererePrietenieValidator());
        networkService = new NetworkService(userRepo, friendshipRepo);
        cerereService = new CererePrietenieService(requestRepo, userRepo, networkService);

        u1 = networkService.addUser(new Utilizator("Ana", "Popescu"));
        u2 = networkService.addUser(new Utilizator("Ion", "Ionescu"));
        u3 = networkService.addUser(new Utilizator("Maria", "Radu"));
    }

    @Test
    void testSendAndApproveFriendRequestCreatesFriendship() {
        AtomicInteger networkEvents = new AtomicInteger(0);
        AtomicInteger requestEvents = new AtomicInteger(0);
        networkService.addObserver(e -> networkEvents.incrementAndGet());
        cerereService.addObserver(e -> requestEvents.incrementAndGet());

        CererePrietenie sent = cerereService.sendRequest(u1.getId(), u2.getId());
        assertNotNull(sent.getId());
        assertEquals(FriendRequestStatus.PENDING, sent.getStatus());
        assertEquals(1, requestEvents.get());

        List<CererePrietenie> received = cerereService.getReceivedRequests(u2.getId());
        assertEquals(1, received.size());
        assertEquals(u1.getId(), received.get(0).getFrom());

        CererePrietenie approved = cerereService.acceptRequest(sent.getId(), u2.getId());
        assertEquals(FriendRequestStatus.APPROVED, approved.getStatus());
        assertTrue(networkService.areFriends(u1.getId(), u2.getId()));
        assertEquals(1, networkEvents.get());
        assertEquals(2, requestEvents.get());
    }

    @Test
    void testSendAndRejectDoesNotCreateFriendship() {
        CererePrietenie sent = cerereService.sendRequest(u1.getId(), u2.getId());
        CererePrietenie rejected = cerereService.rejectRequest(sent.getId(), u2.getId());

        assertEquals(FriendRequestStatus.REJECTED, rejected.getStatus());
        assertFalse(networkService.areFriends(u1.getId(), u2.getId()));
    }

    @Test
    void testBusinessRuleGuards() {
        // Cannot send to self
        assertThrows(IllegalArgumentException.class, () -> cerereService.sendRequest(u1.getId(), u1.getId()));

        // Send valid request u1 -> u2
        CererePrietenie req = cerereService.sendRequest(u1.getId(), u2.getId());

        // Duplicate PENDING in same direction
        assertThrows(IllegalArgumentException.class, () -> cerereService.sendRequest(u1.getId(), u2.getId()));

        // Duplicate PENDING in reverse direction
        assertThrows(IllegalArgumentException.class, () -> cerereService.sendRequest(u2.getId(), u1.getId()));

        // Only recipient can respond (sender or third party cannot)
        assertThrows(IllegalArgumentException.class, () -> cerereService.acceptRequest(req.getId(), u1.getId()));
        assertThrows(IllegalArgumentException.class, () -> cerereService.rejectRequest(req.getId(), u3.getId()));

        // Recipient accepts once
        cerereService.acceptRequest(req.getId(), u2.getId());

        // Cannot respond a second time
        assertThrows(IllegalArgumentException.class, () -> cerereService.rejectRequest(req.getId(), u2.getId()));

        // Cannot send request to existing friend
        assertThrows(IllegalArgumentException.class, () -> cerereService.sendRequest(u1.getId(), u2.getId()));
        assertThrows(IllegalArgumentException.class, () -> cerereService.sendRequest(u2.getId(), u1.getId()));
    }

    @Test
    void testUpdateUserNotifiesObservers() {
        AtomicInteger events = new AtomicInteger(0);
        networkService.addObserver(e -> events.incrementAndGet());

        Utilizator updated = networkService.updateUser(u1.getId(), "Elena", "Popescu");
        assertEquals("Elena", updated.getFirstName());
        assertEquals("Elena", networkService.findUser(u1.getId()).orElseThrow().getFirstName());
        assertEquals(1, events.get());
    }
}
