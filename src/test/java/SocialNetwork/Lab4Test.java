package SocialNetwork;

import SocialNetwork.domain.CriteriiUtilizator;
import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryFactory;
import SocialNetwork.repository.memory.FriendshipRepository;
import SocialNetwork.repository.memory.InMemoryRepository;
import SocialNetwork.service.GraphService;
import SocialNetwork.service.NetworkService;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.validation.UtilizatorValidator;
import SocialNetwork.validation.ValidationException;
import SocialNetwork.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class Lab4Test {
    private Repository<Long, Utilizator> userRepo;
    private Repository<Tuple<Long, Long>, Friendship> friendshipRepo;
    private NetworkService networkService;
    private GraphService graphService;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(new UtilizatorValidator());
        friendshipRepo = new FriendshipRepository(new FriendshipValidator());
        networkService = new NetworkService(userRepo, friendshipRepo);
        graphService = new GraphService(userRepo, friendshipRepo);
    }

    @Test
    void testValidatorCompositionWithLambda() {
        Validator<Utilizator> baseValidator = new UtilizatorValidator();
        Validator<Utilizator> minLengthLambda = u -> {
            if (u.getFirstName().length() < 2 || u.getLastName().length() < 2) {
                throw new ValidationException("Names must have at least 2 characters");
            }
        };

        Validator<Utilizator> composed = baseValidator.andThen(minLengthLambda);

        // Valid user
        assertDoesNotThrow(() -> composed.validate(new Utilizator("Dan", "Pop")));

        // Violates base validator (empty name)
        assertThrows(ValidationException.class, () -> composed.validate(new Utilizator("", "Pop")));

        // Violates lambda rule (length < 2)
        assertThrows(ValidationException.class, () -> composed.validate(new Utilizator("A", "Pop")));
    }

    @Test
    void testRepositoryFindAllWithPredicateAndCriteria() {
        Utilizator u1 = new Utilizator("Andrei", "Popescu");
        u1.setId(1L);
        Utilizator u2 = new Utilizator("Mihai", "Popescu");
        u2.setId(2L);
        Utilizator u3 = new Utilizator("Elena", "Ionescu");
        u3.setId(3L);

        userRepo.save(u1);
        userRepo.save(u2);
        userRepo.save(u3);

        // Filter by last name
        List<Utilizator> popescuList = userRepo.findAll(CriteriiUtilizator.hasLastName("Popescu"));
        assertEquals(2, popescuList.size());

        // Combined filter with and
        Predicate<Utilizator> popescuAndIdOver1 = CriteriiUtilizator.hasLastName("Popescu")
                .and(CriteriiUtilizator.hasIdGreaterThan(1L));
        List<Utilizator> filtered = userRepo.findAll(popescuAndIdOver1);
        assertEquals(1, filtered.size());
        assertEquals("Mihai", filtered.get(0).getFirstName());

        // Test static method reference CriteriiUtilizator::faraPrieteni
        List<Utilizator> noFriends = userRepo.findAll(CriteriiUtilizator::faraPrieteni);
        assertEquals(3, noFriends.size());
    }

    @Test
    void testNetworkServiceStreams() {
        Utilizator u1 = networkService.addUser(new Utilizator("Ion", "Pop"));
        Utilizator u2 = networkService.addUser(new Utilizator("Ana", "Radu"));
        Utilizator u3 = networkService.addUser(new Utilizator("Dan", "Stan"));

        networkService.addFriendship(u1.getId(), u2.getId());
        networkService.addFriendship(u1.getId(), u3.getId());

        List<Utilizator> friendsOfU1 = networkService.getFriendsOfUser(u1.getId());
        assertEquals(2, friendsOfU1.size());

        // Test remove user cascade
        networkService.removeUser(u1.getId());
        assertTrue(userRepo.findOne(u1.getId()).isEmpty());
        assertEquals(0, networkService.getFriendIds(u1.getId()).size());
    }

    @Test
    void testGraphServiceCommunitiesAndSociableCommunity() {
        Utilizator u1 = networkService.addUser(new Utilizator("User1", "Test"));
        Utilizator u2 = networkService.addUser(new Utilizator("User2", "Test"));
        Utilizator u3 = networkService.addUser(new Utilizator("User3", "Test"));
        Utilizator u4 = networkService.addUser(new Utilizator("User4", "Test"));

        // Community 1: u1 - u2 - u3 (diameter 2)
        networkService.addFriendship(u1.getId(), u2.getId());
        networkService.addFriendship(u2.getId(), u3.getId());

        // Community 2: u4 isolated (diameter 0)
        assertEquals(2, graphService.getNumberOfCommunities());

        List<Utilizator> sociable = graphService.getMostSociableCommunity();
        assertEquals(3, sociable.size());
    }

    @Test
    void testRepositoryFactoryMapDispatch() {
        RepositoryFactory factory = new RepositoryFactory();
        Repository<Long, Utilizator> repo = factory.createUserRepository(new UtilizatorValidator());
        assertNotNull(repo);
    }
}
