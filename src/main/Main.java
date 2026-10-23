package main;

import domain.Friendship;
import Validation.FriendshipValidator;
import domain.Tuple;
import domain.Utilizator;
import Validation.UtilizatorValidator;
import Validation.Validator;
import repository.Repository;
import repository.RepositoryFactory;
import service.GraphService;
import service.NetworkService;
import ui.UI;

/**
 * Main application entry point.
 */
public class Main {
    /**
     * Main method that initializes application components and starts the console interface.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Validator<Utilizator> userValidator = new UtilizatorValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();

        RepositoryFactory factory = new RepositoryFactory();
        Repository<Long, Utilizator> userRepo = factory.createUserRepository(userValidator);
        Repository<Tuple<Long, Long>, Friendship> friendshipRepo = factory.createFriendshipRepository(friendshipValidator);

        NetworkService service = new NetworkService(userRepo, friendshipRepo);
        GraphService graphService = new GraphService(userRepo, friendshipRepo);
        UI ui = new UI(service, graphService);

        ui.run();
    }
}
