package SocialNetwork.controller;

import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.RepositoryException;
import SocialNetwork.service.NetworkService;
import SocialNetwork.service.ServiceException;
import SocialNetwork.validation.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the user add/edit dialog window, modeled after {@code EditMessageTaskController}.
 * Receives the selected {@link Utilizator} ({@code null} when adding a new user,
 * or the existing user when editing).
 */
public class UserFormController {

    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button saveButton;

    private NetworkService service;
    private Stage dialogStage;
    private Utilizator utilizator;

    /**
     * Injects the {@link NetworkService}, the dialog {@link Stage}, and the {@link Utilizator} to edit.
     *
     * @param service     the network service handling user persistence
     * @param dialogStage the modal stage hosting this form
     * @param utilizator  the user to edit, or {@code null} to create a new user
     */
    public void setService(NetworkService service, Stage dialogStage, Utilizator utilizator) {
        this.service = service;
        this.dialogStage = dialogStage;
        this.utilizator = utilizator;

        if (utilizator != null) {
            formTitleLabel.setText("Modifică utilizator");
            firstNameField.setText(utilizator.getFirstName());
            lastNameField.setText(utilizator.getLastName());
            saveButton.setText("Actualizează");
        } else {
            formTitleLabel.setText("Adaugă utilizator");
            saveButton.setText("Adaugă");
        }
    }

    /**
     * Handles saving either a new user or updates to an existing user,
     * displaying domain, validation, or persistence errors via {@link MessageAlert}.
     */
    @FXML
    private void handleSave() {
        String firstName = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField.getText() != null ? lastNameField.getText().trim() : "";

        try {
            if (utilizator == null) {
                service.addUser(new Utilizator(firstName, lastName));
            } else {
                service.updateUser(utilizator.getId(), firstName, lastName);
            }
            dialogStage.close();
        } catch (ValidationException | RepositoryException | ServiceException | IllegalArgumentException e) {
            MessageAlert.showErrorMessage(dialogStage, e.getMessage());
        }
    }

    /**
     * Cancels the operation and closes the dialog window.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
