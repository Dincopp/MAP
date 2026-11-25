package SocialNetwork.controller;

import SocialNetwork.domain.CererePrietenie;
import SocialNetwork.domain.FriendRequestStatus;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.repository.util.Page;
import SocialNetwork.service.CererePrietenieService;
import SocialNetwork.service.NetworkService;
import SocialNetwork.service.ServiceException;
import SocialNetwork.utils.events.EntityChangeEvent;
import SocialNetwork.utils.observer.Observer;
import SocialNetwork.validation.ValidationException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Main JavaFX controller for the Social Network application, modeled after Seminar 9 {@code MovieController}.
 * Implements {@link Observer} to automatically refresh its {@link ObservableList} models
 * whenever users, friendships, or friend requests change in the services.
 */
public class SocialNetworkController implements Observer<EntityChangeEvent<?>> {

    private static final int DEFAULT_PAGE_SIZE = 5;

    @FXML
    private TableView<Utilizator> usersTable;

    @FXML
    private TableColumn<Utilizator, Long> userIdColumn;

    @FXML
    private TableColumn<Utilizator, String> userFirstNameColumn;

    @FXML
    private TableColumn<Utilizator, String> userLastNameColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button updateUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button prevUsersButton;

    @FXML
    private Button nextUsersButton;

    @FXML
    private Label usersPageLabel;

    @FXML
    private Label currentUserLabel;

    @FXML
    private TableView<Utilizator> friendsTable;

    @FXML
    private TableColumn<Utilizator, Long> friendIdColumn;

    @FXML
    private TableColumn<Utilizator, String> friendFirstNameColumn;

    @FXML
    private TableColumn<Utilizator, String> friendLastNameColumn;

    @FXML
    private Button prevFriendsButton;

    @FXML
    private Button nextFriendsButton;

    @FXML
    private Label friendsPageLabel;

    @FXML
    private Button removeFriendButton;

    @FXML
    private ComboBox<Utilizator> targetUserBox;

    @FXML
    private Button sendRequestButton;

    @FXML
    private TableView<CererePrietenie> requestsTable;

    @FXML
    private TableColumn<CererePrietenie, Long> requestIdColumn;

    @FXML
    private TableColumn<CererePrietenie, String> requestFromColumn;

    @FXML
    private TableColumn<CererePrietenie, String> requestStatusColumn;

    @FXML
    private TableColumn<CererePrietenie, String> requestDateColumn;

    @FXML
    private Button acceptRequestButton;

    @FXML
    private Button rejectRequestButton;

    private final ObservableList<Utilizator> usersModel = FXCollections.observableArrayList();
    private final ObservableList<Utilizator> friendsModel = FXCollections.observableArrayList();
    private final ObservableList<CererePrietenie> requestsModel = FXCollections.observableArrayList();
    private final ObservableList<Utilizator> allUsersModel = FXCollections.observableArrayList();

    private NetworkService networkService;
    private CererePrietenieService cerereService;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private int usersPageNumber = 0;
    private int usersTotalPages = 0;
    private int friendsPageNumber = 0;
    private int friendsTotalPages = 0;
    private Long selectedUserId = null;
    private boolean reloadingUsers = false;

    /**
     * Initializes table columns, selection bindings, and cell factories after FXML injection.
     */
    @FXML
    private void initialize() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usersTable.setItems(usersModel);

        friendIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        friendFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendsTable.setItems(friendsModel);

        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        requestFromColumn.setCellValueFactory(cellData -> {
            Long fromId = cellData.getValue().getFrom();
            String display = resolveUserDisplayName(fromId);
            return new SimpleStringProperty(display);
        });
        requestStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().name()));
        requestStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-pending", "status-approved", "status-rejected");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDING" -> getStyleClass().add("status-pending");
                        case "APPROVED" -> getStyleClass().add("status-approved");
                        case "REJECTED" -> getStyleClass().add("status-rejected");
                        default -> {
                        }
                    }
                }
            }
        });
        requestDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        requestsTable.setItems(requestsModel);

        targetUserBox.setItems(allUsersModel);
        targetUserBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Utilizator u) {
                if (u == null) {
                    return "";
                }
                return u.getId() + " - " + u.getFirstName() + " " + u.getLastName();
            }

            @Override
            public Utilizator fromString(String string) {
                return null;
            }
        });

        BooleanBinding noUserSelected = usersTable.getSelectionModel().selectedItemProperty().isNull();
        updateUserButton.disableProperty().bind(noUserSelected);
        deleteUserButton.disableProperty().bind(noUserSelected);
        sendRequestButton.disableProperty().bind(noUserSelected.or(targetUserBox.valueProperty().isNull()));

        BooleanBinding noFriendSelected = friendsTable.getSelectionModel().selectedItemProperty().isNull();
        removeFriendButton.disableProperty().bind(noFriendSelected);

        BooleanBinding noRequestSelected = requestsTable.getSelectionModel().selectedItemProperty().isNull();
        acceptRequestButton.disableProperty().bind(noRequestSelected);
        rejectRequestButton.disableProperty().bind(noRequestSelected);

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (reloadingUsers) {
                return;
            }
            if (newSelection != null) {
                selectedUserId = newSelection.getId();
                friendsPageNumber = 0;
            } else {
                selectedUserId = null;
            }
            refreshCurrentUserPanels();
        });
    }

    /**
     * Injects the application services and page size via setter, registers this controller as an observer,
     * and performs the initial data load.
     *
     * @param networkService the service managing users and friendships
     * @param cerereService  the service managing friend requests
     * @param pageSize       the number of items per page
     */
    public void setServices(NetworkService networkService, CererePrietenieService cerereService, int pageSize) {
        this.networkService = networkService;
        this.cerereService = cerereService;
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }

        this.networkService.addObserver(this);
        this.cerereService.addObserver(this::update);

        reloadAll();
    }

    /**
     * Observer callback triggered whenever any user, friendship, or friend request changes.
     * Reloads observable models to keep the UI synchronized.
     *
     * @param event the change event emitted by an observed service
     */
    @Override
    public void update(EntityChangeEvent<?> event) {
        reloadAll();
    }

    /**
     * Reloads all tables and selector data from the services.
     */
    private void reloadAll() {
        try {
            loadAllUsersForSelector();
            loadUsersPage();
            refreshCurrentUserPanels();
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(getStage(), e.getMessage());
        }
    }

    /**
     * Populates the target user selector combo box with all users in the network.
     */
    private void loadAllUsersForSelector() {
        Utilizator previousTarget = targetUserBox.getValue();
        List<Utilizator> allUsers = StreamSupport.stream(networkService.getAllUsers().spliterator(), false)
                .collect(Collectors.toList());
        allUsersModel.setAll(allUsers);
        if (previousTarget != null && previousTarget.getId() != null) {
            allUsers.stream()
                    .filter(u -> Objects.equals(u.getId(), previousTarget.getId()))
                    .findFirst()
                    .ifPresent(targetUserBox::setValue);
        }
    }

    /**
     * Loads the current page of users and updates pagination controls and table selection.
     */
    private void loadUsersPage() {
        Page<Utilizator> page = networkService.getUsersOnPage(usersPageNumber, pageSize);
        int totalElements = page.getTotalNumberOfElements();
        usersTotalPages = (totalElements + pageSize - 1) / pageSize;

        if (usersTotalPages > 0 && usersPageNumber >= usersTotalPages) {
            usersPageNumber = usersTotalPages - 1;
            page = networkService.getUsersOnPage(usersPageNumber, pageSize);
        } else if (usersTotalPages == 0) {
            usersPageNumber = 0;
        }

        List<Utilizator> elements = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());

        reloadingUsers = true;
        try {
            usersModel.setAll(elements);
            if (selectedUserId != null) {
                elements.stream()
                        .filter(u -> Objects.equals(u.getId(), selectedUserId))
                        .findFirst()
                        .ifPresent(u -> usersTable.getSelectionModel().select(u));
            }
        } finally {
            reloadingUsers = false;
        }

        updatePaginationLabel(usersPageLabel, prevUsersButton, nextUsersButton, usersPageNumber, usersTotalPages);
    }

    /**
     * Refreshes the friends table (paginated) and received friend requests table for the currently selected user.
     */
    private void refreshCurrentUserPanels() {
        if (selectedUserId == null) {
            setNoCurrentUserState();
            return;
        }

        Optional<Utilizator> currentOpt = networkService.findUser(selectedUserId);
        if (currentOpt.isEmpty()) {
            selectedUserId = null;
            setNoCurrentUserState();
            return;
        }

        Utilizator current = currentOpt.get();
        currentUserLabel.setText("Utilizator curent: " + current.getFirstName() + " " + current.getLastName()
                + " (ID: " + current.getId() + ")");

        loadFriendsPage();
        loadRequests();
    }

    /**
     * Resets the current-user right-side panels when no user is selected.
     */
    private void setNoCurrentUserState() {
        currentUserLabel.setText("Utilizator curent: selectați un utilizator din tabel");
        friendsModel.clear();
        requestsModel.clear();
        friendsPageNumber = 0;
        friendsTotalPages = 0;
        updatePaginationLabel(friendsPageLabel, prevFriendsButton, nextFriendsButton, 0, 0);
    }

    /**
     * Loads the current page of friends for the selected user.
     */
    private void loadFriendsPage() {
        if (selectedUserId == null) {
            return;
        }
        Page<Utilizator> page = networkService.getFriendsOnPage(selectedUserId, friendsPageNumber, pageSize);
        int totalElements = page.getTotalNumberOfElements();
        friendsTotalPages = (totalElements + pageSize - 1) / pageSize;

        if (friendsTotalPages > 0 && friendsPageNumber >= friendsTotalPages) {
            friendsPageNumber = friendsTotalPages - 1;
            page = networkService.getFriendsOnPage(selectedUserId, friendsPageNumber, pageSize);
        } else if (friendsTotalPages == 0) {
            friendsPageNumber = 0;
        }

        List<Utilizator> friends = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());
        friendsModel.setAll(friends);

        updatePaginationLabel(friendsPageLabel, prevFriendsButton, nextFriendsButton, friendsPageNumber, friendsTotalPages);
    }

    /**
     * Loads all received friend requests for the currently selected user.
     */
    private void loadRequests() {
        if (selectedUserId == null) {
            return;
        }
        List<CererePrietenie> received = cerereService.getReceivedRequests(selectedUserId);
        requestsModel.setAll(received);
    }

    /**
     * Updates a pagination label ("Pagina x din y") and enables/disables navigation buttons accordingly.
     *
     * @param label      the label displaying page progress
     * @param prevButton the previous-page button
     * @param nextButton the next-page button
     * @param pageNumber the 0-based current page index
     * @param totalPages the total number of pages
     */
    private void updatePaginationLabel(Label label, Button prevButton, Button nextButton, int pageNumber, int totalPages) {
        if (totalPages == 0) {
            label.setText("Pagina 0 din 0");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
        } else {
            label.setText("Pagina " + (pageNumber + 1) + " din " + totalPages);
            prevButton.setDisable(pageNumber <= 0);
            nextButton.setDisable(pageNumber + 1 >= totalPages);
        }
    }

    /**
     * Resolves a user ID to a readable full name with ID for display in the requests table.
     *
     * @param userId the user identifier
     * @return formatted user display string
     */
    private String resolveUserDisplayName(Long userId) {
        if (userId == null) {
            return "";
        }
        return networkService.findUser(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName() + " (ID: " + u.getId() + ")")
                .orElse("ID: " + userId);
    }

    /**
     * Navigates to the previous page of users.
     */
    @FXML
    private void onUsersPrevious() {
        if (usersPageNumber > 0) {
            usersPageNumber--;
            try {
                loadUsersPage();
            } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
                MessageAlert.showErrorMessage(getStage(), e.getMessage());
            }
        }
    }

    /**
     * Navigates to the next page of users.
     */
    @FXML
    private void onUsersNext() {
        if (usersPageNumber + 1 < usersTotalPages) {
            usersPageNumber++;
            try {
                loadUsersPage();
            } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
                MessageAlert.showErrorMessage(getStage(), e.getMessage());
            }
        }
    }

    /**
     * Navigates to the previous page of friends for the current user.
     */
    @FXML
    private void onFriendsPrevious() {
        if (friendsPageNumber > 0) {
            friendsPageNumber--;
            try {
                loadFriendsPage();
            } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
                MessageAlert.showErrorMessage(getStage(), e.getMessage());
            }
        }
    }

    /**
     * Navigates to the next page of friends for the current user.
     */
    @FXML
    private void onFriendsNext() {
        if (friendsPageNumber + 1 < friendsTotalPages) {
            friendsPageNumber++;
            try {
                loadFriendsPage();
            } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
                MessageAlert.showErrorMessage(getStage(), e.getMessage());
            }
        }
    }

    /**
     * Opens the user add/edit dialog in add mode ({@code user = null}).
     */
    @FXML
    private void handleAddUser() {
        openUserForm(null);
    }

    /**
     * Opens the user add/edit dialog pre-populated with the currently selected user.
     */
    @FXML
    private void handleUpdateUser() {
        Utilizator selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un utilizator din tabel!");
            return;
        }
        openUserForm(selected);
    }

    /**
     * Deletes the currently selected user after asking for confirmation.
     */
    @FXML
    private void handleDeleteUser() {
        Utilizator selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un utilizator din tabel!");
            return;
        }

        boolean confirmed = MessageAlert.showConfirmation(
                getStage(),
                "Confirmare ștergere",
                "Ștergere utilizator",
                "Sigur doriți să ștergeți utilizatorul " + selected.getFirstName() + " " + selected.getLastName()
                        + " (ID: " + selected.getId() + ")?"
        );
        if (!confirmed) {
            return;
        }

        try {
            networkService.removeUser(selected.getId());
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(getStage(), e.getMessage());
        }
    }

    /**
     * Removes the selected friendship for the current user after confirmation.
     */
    @FXML
    private void handleRemoveFriend() {
        if (selectedUserId == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un utilizator curent!");
            return;
        }
        Utilizator selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedFriend == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un prieten din listă!");
            return;
        }

        boolean confirmed = MessageAlert.showConfirmation(
                getStage(),
                "Confirmare ștergere prietenie",
                "Ștergere prietenie",
                "Sigur doriți să ștergeți prietenia cu " + selectedFriend.getFirstName() + " " + selectedFriend.getLastName() + "?"
        );
        if (!confirmed) {
            return;
        }

        try {
            networkService.removeFriendship(selectedUserId, selectedFriend.getId());
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(getStage(), e.getMessage());
        }
    }

    /**
     * Sends a friend request from the currently selected user to the chosen target user.
     */
    @FXML
    private void handleSendRequest() {
        if (selectedUserId == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un utilizator curent!");
            return;
        }
        Utilizator target = targetUserBox.getValue();
        if (target == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați destinatarul cererii de prietenie!");
            return;
        }

        try {
            cerereService.sendRequest(selectedUserId, target.getId());
            MessageAlert.showMessage(
                    getStage(),
                    Alert.AlertType.INFORMATION,
                    "Cerere trimisă",
                    "Cererea de prietenie către " + target.getFirstName() + " " + target.getLastName() + " a fost trimisă!"
            );
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(getStage(), e.getMessage());
        }
    }

    /**
     * Approves the selected received friend request for the current user.
     */
    @FXML
    private void handleAcceptRequest() {
        respondToSelectedRequest(FriendRequestStatus.APPROVED);
    }

    /**
     * Rejects the selected received friend request for the current user.
     */
    @FXML
    private void handleRejectRequest() {
        respondToSelectedRequest(FriendRequestStatus.REJECTED);
    }

    /**
     * Dispatches an approval or rejection response for the currently selected friend request.
     *
     * @param status {@link FriendRequestStatus#APPROVED} or {@link FriendRequestStatus#REJECTED}
     */
    private void respondToSelectedRequest(FriendRequestStatus status) {
        if (selectedUserId == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați un utilizator curent!");
            return;
        }
        CererePrietenie selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            MessageAlert.showErrorMessage(getStage(), "Selectați o cerere de prietenie!");
            return;
        }

        try {
            cerereService.respondToRequest(selectedRequest.getId(), selectedUserId, status);
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(getStage(), e.getMessage());
        }
    }

    /**
     * Loads and opens the modal user form dialog for adding or editing a {@link Utilizator}.
     *
     * @param utilizator the user to edit, or {@code null} to add a new user
     */
    private void openUserForm(Utilizator utilizator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SocialNetwork/user-form-view.fxml"));
            AnchorPane root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(utilizator == null ? "Adaugă utilizator" : "Modifică utilizator");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage owner = getStage();
            if (owner != null) {
                dialogStage.initOwner(owner);
            }

            Scene scene = new Scene(root);
            var cssUrl = getClass().getResource("/SocialNetwork/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            dialogStage.setScene(scene);

            UserFormController controller = loader.getController();
            controller.setService(networkService, dialogStage, utilizator);

            dialogStage.showAndWait();
        } catch (IOException e) {
            MessageAlert.showErrorMessage(getStage(), "Nu s-a putut deschide fereastra de editare: " + e.getMessage());
        }
    }

    /**
     * Returns the {@link Stage} hosting the main view.
     *
     * @return the owner {@link Stage}, or {@code null} if not yet attached to a window
     */
    private Stage getStage() {
        if (usersTable != null && usersTable.getScene() != null && usersTable.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        return null;
    }
}
