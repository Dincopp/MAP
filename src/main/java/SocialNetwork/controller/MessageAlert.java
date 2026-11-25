package SocialNetwork.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for displaying JavaFX alert dialogs (information, error, and confirmation)
 * across application controllers.
 */
public final class MessageAlert {

    private MessageAlert() {
    }

    /**
     * Displays an alert dialog of the specified type with a header and message body.
     *
     * @param owner  the parent {@link Stage} for modality, or {@code null}
     * @param type   the {@link Alert.AlertType} to display
     * @param header the header text of the alert
     * @param text   the detailed content message
     */
    public static void showMessage(Stage owner, Alert.AlertType type, String header, String text) {
        Alert message = new Alert(type);
        message.setHeaderText(header);
        message.setContentText(text);
        if (owner != null) {
            message.initOwner(owner);
        }
        message.showAndWait();
    }

    /**
     * Displays an error alert dialog with a specific error message.
     *
     * @param owner the parent {@link Stage} for modality, or {@code null}
     * @param text  the specific error message to display
     */
    public static void showErrorMessage(Stage owner, String text) {
        Alert message = new Alert(Alert.AlertType.ERROR);
        if (owner != null) {
            message.initOwner(owner);
        }
        message.setTitle("Eroare");
        message.setHeaderText("A apărut o eroare");
        message.setContentText(text);
        message.showAndWait();
    }

    /**
     * Displays a confirmation dialog and returns whether the user clicked OK.
     *
     * @param owner  the parent {@link Stage} for modality, or {@code null}
     * @param title  the window title of the confirmation dialog
     * @param header the header text of the dialog
     * @param text   the confirmation prompt message
     * @return {@code true} if the user confirmed with OK, {@code false} otherwise
     */
    public static boolean showConfirmation(Stage owner, String title, String header, String text) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) {
            confirmation.initOwner(owner);
        }
        confirmation.setTitle(title);
        confirmation.setHeaderText(header);
        confirmation.setContentText(text);
        Optional<ButtonType> result = confirmation.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
