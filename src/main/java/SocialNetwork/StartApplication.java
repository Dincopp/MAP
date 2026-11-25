package SocialNetwork;

import SocialNetwork.controller.MessageAlert;
import SocialNetwork.controller.SocialNetworkController;
import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.domain.Friendship;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.repository.RepositoryFactory;
import SocialNetwork.service.CererePrietenieService;
import SocialNetwork.service.NetworkService;
import SocialNetwork.validation.CererePrietenieValidator;
import SocialNetwork.validation.FriendshipValidator;
import SocialNetwork.validation.UtilizatorValidator;
import SocialNetwork.validation.ValidationException;
import SocialNetwork.validation.Validator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX entry point for the Social Network application.
 * Initializes validators, repositories via {@link RepositoryFactory}, and observable services,
 * then loads the main FXML view and injects services into {@link SocialNetworkController} via setter.
 */
public class StartApplication extends Application {

    /**
     * Initializes the primary JavaFX stage, constructs services through {@link RepositoryFactory},
     * and wires the main controller.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            Validator<Utilizator> baseUserValidator = new UtilizatorValidator();
            Validator<Utilizator> nameLengthRule = u -> {
                if (u.getFirstName().length() < 2 || u.getLastName().length() < 2) {
                    throw new ValidationException("First and last names must contain at least 2 characters!");
                }
            };
            Validator<Utilizator> userValidator = baseUserValidator.andThen(nameLengthRule);
            Validator<Friendship> friendshipValidator = new FriendshipValidator();
            Validator<CererePrietenie> requestValidator = new CererePrietenieValidator();

            RepositoryFactory factory = new RepositoryFactory();
            Repository<Long, Utilizator> userRepo = factory.createUserRepository(userValidator);
            Repository<Tuple<Long, Long>, Friendship> friendshipRepo = factory.createFriendshipRepository(friendshipValidator);
            Repository<Long, CererePrietenie> requestRepo = factory.createFriendRequestRepository(requestValidator);

            NetworkService networkService = new NetworkService(userRepo, friendshipRepo);
            CererePrietenieService cerereService = new CererePrietenieService(requestRepo, userRepo, networkService);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SocialNetwork/main-view.fxml"));
            Scene scene = new Scene(loader.load());
            var cssUrl = getClass().getResource("/SocialNetwork/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            SocialNetworkController controller = loader.getController();
            controller.setServices(networkService, cerereService, factory.getPageSize());

            primaryStage.setTitle("Rețea de Socializare");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (RepositoryException | IOException e) {
            MessageAlert.showErrorMessage(primaryStage, "Aplicația nu a putut fi inițializată: " + e.getMessage());
        }
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
