package tim.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A reusable banner-like control to display a reminder with a dismiss button.
 * It stays hidden by default (managed=false, visible=false) and can be shown
 * with a message and an optional onDismiss action.
 */
public class ReminderBox extends HBox {

    @FXML private Label messageLabel;
    @FXML private Button dismissButton;

    /** Optional callback invoked when user clicks Dismiss. */
    private Runnable onDismiss = () -> { };

    /**
     * Constructs a {@code ReminderBox} by loading its FXML layout
     * and wiring up the dismiss button. Hidden by default until shown.
     */
    public ReminderBox() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ReminderBox.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
            dismissButton.setOnAction(e -> handleDismiss());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load ReminderBox.fxml", e);
        }
        getStyleClass().add("reminder-box"); // CSS hook for this control
        // Hidden by default
        setVisible(false);
        setManaged(false);
    }

    /**
     * Shows the reminder with the given message and a dismiss callback.
     * @param message text to display in the banner
     * @param onDismiss invoked when Dismiss is clicked (nullable)
     */
    public void show(String message, Runnable onDismiss) {
        messageLabel.setText(message);
        this.onDismiss = (onDismiss != null) ? onDismiss : () -> { };
        setManaged(true);
        setVisible(true);
    }

    /** Hides the reminder box. */
    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    /** Handles the Dismiss button click from FXML. */
    @FXML
    private void handleDismiss() {
        hide();
        onDismiss.run();
    }
}
