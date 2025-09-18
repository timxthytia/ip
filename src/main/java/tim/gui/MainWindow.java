package tim.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tim.app.Tim;
import tim.reminder.ReminderService;
import tim.reminder.ReminderService.ReminderEvent;


/** Controller for MainWindow.fxml. Handles initialization and user interactions. */
public class MainWindow extends AnchorPane {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox dialogContainer;
    @FXML private TextField userInput;
    @FXML private Button sendButton;
    @FXML private ReminderBox reminderBox;
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
        getStyleClass().add("root-pane");
        dialogContainer.getChildren().add(
                DialogBox.getTimDialog("Welcome to Tim! How can I help you today?", timImage)
        );
        if (reminderBox != null) {
            reminderBox.setVisible(false);
            reminderBox.setManaged(false);
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
                this::showReminder);
        reminderService.start();
    }

    /**
     * Displays a reminder message in the ReminderBox.
     * Safe to call from any thread.
     */
    private void showReminder(ReminderEvent evt) {
        if (reminderBox == null) {
            return; // Fallback if FXML node is not present
        }
        Platform.runLater(() -> {
            reminderBox.show(evt.getTaskLabel(), () -> {
                if (reminderService != null) {
                    // Dismiss the event.
                    reminderService.dismiss(evt);
                }
            });
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

        // Check if response is an error
        boolean isError = response.startsWith("OOPS!!!");

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                isError ? DialogBox.getErrorDialog(response, timImage)
                        : DialogBox.getTimDialog(response, timImage)
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
