package SocialNetwork.main;

import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.validation.UtilizatorValidator;
import SocialNetwork.validation.ValidationException;
import SocialNetwork.validation.Validator;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryFactory;
import SocialNetwork.service.GraphService;
import SocialNetwork.service.NetworkService;
import SocialNetwork.ui.UI;

/**
 * Main application entry point demonstrating functional composition of validators via {@link Validator#andThen(Validator)}.
 */
public class Main {
    /**
     * Main method that initializes application components and starts the console interface.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Base user validator
        Validator<Utilizator> baseUserValidator = new UtilizatorValidator();

        // New validation rule written as a lambda expression without modifying UtilizatorValidator or creating a class
        Validator<Utilizator> nameLengthRule = u -> {
            if (u.getFirstName().length() < 2 || u.getLastName().length() < 2) {
                throw new ValidationException("First and last names must contain at least 2 characters!");
            }
        };

        // Composed validator using the default method andThen
        Validator<Utilizator> userValidator = baseUserValidator.andThen(nameLengthRule);
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
