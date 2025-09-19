package tim.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A reusable banner-like control to display a reminder with a dismiss button.
 * Hidden by default (managed=false, visible=false). Call {@link #show(String, Runnable)} to display.
 */
public final class ReminderBox extends HBox {
    private static final String FXML_PATH = "/view/ReminderBox.fxml";
    private static final String STYLE_CLASS = "reminder-box";
    private static final String ERR_FXML = "Failed to load ReminderBox.fxml";
    private static final String ERR_LABEL_NOT_INJECTED = "messageLabel not injected";
    private static final String ERR_BUTTON_NOT_INJECTED = "dismissButton not injected";
    private static final String EMPTY = "";

    /** Optional callback invoked when user clicks Dismiss. */
    private static final Runnable NO_OP = () -> { };
    private Runnable onDismiss = NO_OP;
    @FXML private Label messageLabel;
    @FXML private Button dismissButton;

    /**
     * Constructs a {@code ReminderBox} by loading its FXML layout
     * and wiring up the dismiss button. Hidden by default until shown.
     */
    public ReminderBox() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
            assert messageLabel != null : ERR_LABEL_NOT_INJECTED;
            assert dismissButton != null : ERR_BUTTON_NOT_INJECTED;
            dismissButton.setOnAction(e -> handleDismiss());
        } catch (IOException e) {
            throw new IllegalStateException(ERR_FXML, e);
        }
        getStyleClass().add(STYLE_CLASS); // CSS hook for this control
        // Hidden by default
        setVisible(false);
        setManaged(false);
    }

    /**
     * Shows the reminder with the given message and a dismiss callback.
     *
     * @param message text to display in the banner; null treated as empty, trimmed
     * @param onDismiss invoked when Dismiss is clicked; nullable (no-op if null)
     */
    public void show(String message, Runnable onDismiss) {
        String text = (message == null) ? EMPTY : message.trim();
        messageLabel.setText(text);
        this.onDismiss = (onDismiss != null) ? onDismiss : NO_OP;
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
        try {
            onDismiss.run();
        } catch (RuntimeException ex) {
            // Swallow to avoid crashing UI; log if you have a logger.
        } finally {
            onDismiss = NO_OP; // prevent accidental reuse
        }
    }
}
