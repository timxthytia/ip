package tim.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tim.app.Tim;
import tim.reminder.ReminderService;


/** Controller for MainWindow.fxml. Handles initialization and user interactions. */
public class MainWindow extends AnchorPane {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox dialogContainer;
    @FXML private TextField userInput;
    @FXML private Button sendButton;
    @FXML private HBox reminderBar;
    @FXML private Label reminderLabel;
    private ReminderService reminderService;

    private Tim tim;
    private final Image userImage = new Image(getClass().getResourceAsStream("/images/DaUser.png"));
    private final Image timImage = new Image(getClass().getResourceAsStream("/images/DaDuke.png"));

    /**
     * Initializes the controller class. Sets up auto-scrolling and displays a welcome message.
     */
    @FXML
    private void initialize() {
        // Auto-scroll to the bottom when new messages arrive
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        dialogContainer.getChildren().add(
                DialogBox.getTimDialog("Welcome to Tim! How can I help you today?", timImage)
        );
        if (reminderBar != null) {
            reminderBar.setVisible(false);
            reminderBar.setManaged(false);
        }
    }

    /**
     * Injects the Tim instance to be used by the controller.
     * @param tim Tim instance to handle user input and generate responses.
     */
    public void setTim(Tim tim) {
        this.tim = tim;
        // Wire and start reminder scanning based on current tasks
        reminderService = new ReminderService(this.tim.getTaskList(),
                evt -> showReminder(evt.getTaskLabel()));
        reminderService.start();
    }

    /**
     * Displays a reminder message in the top reminder bar.
     * This method is safe to call from any thread.
     */
    private void showReminder(String message) {
        if (reminderBar == null || reminderLabel == null) {
            return; // Fallback if FXML nodes are not present
        }
        Platform.runLater(() -> {
            reminderLabel.setText(message);
            reminderBar.setVisible(true);
            reminderBar.setManaged(true);
        });
    }

    /**
     * Handles user input by creating two dialog boxes: one for the user input and one for Tim's response.
     * Adds both dialog boxes to the dialog container and clears the user input field.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input == null || input.isBlank()) {
            return;
        }

        String response = tim.getResponse(input);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getTimDialog(response, timImage)
        );

        userInput.clear();

        if ("bye".equals(input.trim())) {
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(e -> {
                if (reminderService != null) {
                    reminderService.stop();
                }
                Platform.exit();
            });
            delay.play();
        }
    }
}
