package tim.gui;

import java.util.Objects;

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

/**
 * Controller for the main chat window. Wires UI components, renders dialog bubbles,
 * mediates between user input and the core {@link tim.app.Tim} logic, and handles reminders.
 */
public class MainWindow extends AnchorPane {
    private static final String USER_IMG_PATH = "/images/DaUser.png";
    private static final String TIM_IMG_PATH = "/images/DaDuke.png";
    private static final String WELCOME_MSG = "Welcome to Tim! How can I help you today?";
    private static final String ERROR_PREFIX = "OOPS!!!";
    private static final String STYLE_ROOT_PANE = "root-pane";
    private static final String MISSING_RESOURCE_PREFIX = "Missing resource: ";
    private static final String BYE_COMMAND = "bye";
    private static final int EXIT_DELAY_MS = 200;
    private static final String ERR_SCROLL_NOT_INJECTED = "scrollPane not injected";
    private static final String ERR_DIALOG_NOT_INJECTED = "dialogContainer not injected";
    private static final String ERR_USER_INPUT_NOT_INJECTED = "userInput not injected";
    private static final String ERR_SEND_NOT_INJECTED = "sendButton not injected";
    private static final String EMPTY = "";
    @FXML private ScrollPane scrollPane;
    @FXML private VBox dialogContainer;
    @FXML private TextField userInput;
    @FXML private Button sendButton;
    @FXML private ReminderBox reminderBox;
    private ReminderService reminderService;
    private Tim tim;
    private final Image userImage = new Image(Objects.requireNonNull(
            getClass().getResourceAsStream(USER_IMG_PATH),
            MISSING_RESOURCE_PREFIX + USER_IMG_PATH));
    private final Image timImage = new Image(Objects.requireNonNull(
            getClass().getResourceAsStream(TIM_IMG_PATH),
            MISSING_RESOURCE_PREFIX + TIM_IMG_PATH));

    /**
     * Initializes the controller class. Sets up auto-scrolling, base styling, and displays a welcome message.
     * @throws IllegalStateException if FXML injection failed for critical nodes
     */
    @FXML
    private void initialize() {
        // FXML injection sanity checks
        assert scrollPane != null : ERR_SCROLL_NOT_INJECTED;
        assert dialogContainer != null : ERR_DIALOG_NOT_INJECTED;
        assert userInput != null : ERR_USER_INPUT_NOT_INJECTED;
        assert sendButton != null : ERR_SEND_NOT_INJECTED;

        // Auto-scroll to the bottom when new messages arrive
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        // Base styling hook
        getStyleClass().add(STYLE_ROOT_PANE);

        // Welcome message
        dialogContainer.getChildren().add(DialogBox.getTimDialog(WELCOME_MSG, timImage));

        if (reminderBox != null) {
            reminderBox.setVisible(false);
            reminderBox.setManaged(false);
        }
    }

    /**
     * Injects the Tim instance to be used by the controller.
     * @param tim the Tim instance that handles user input and generates responses
     * @throws NullPointerException if {@code tim} is null
     */
    public void setTim(Tim tim) {
        this.tim = Objects.requireNonNull(tim, "tim must not be null");
        // Wire and start reminder scanning based on current tasks
        reminderService = new ReminderService(this.tim.getTaskList(), this::showReminder);
        reminderService.start();
    }

    /**
     * Displays a reminder message in the ReminderBox.
     * Safe to call from any thread.
     * @param evt the reminder event to display; ignored if null or if the ReminderBox is absent
     */
    private void showReminder(ReminderEvent evt) {
        if (evt == null || reminderBox == null) {
            return; // Fallback if event is null or FXML node is not present
        }
        Platform.runLater(() -> reminderBox.show(evt.getTaskLabel(), () -> {
            if (reminderService != null) {
                reminderService.dismiss(evt); // Dismiss the event
            }
        }));
    }

    /**
     * Handles user input: normalizes text, renders the user bubble, delegates to core for a response,
     * renders Tim's bubble, and processes exit if needed.
     */
    @FXML
    private void handleUserInput() {
        final String input = readNormalizedInput();
        if (input.isEmpty()) {
            return;
        }
        renderUserBubble(input);
        final String response = tim.getResponse(input);
        renderTimBubble(response);
        userInput.clear();
        processExitIfNeeded(input);
    }

    /**
     * Reads and trims the current content of the input field.
     *
     * @return the trimmed input text, or an empty string if null/blank
     */
    private String readNormalizedInput() {
        final String raw = userInput.getText();
        return (raw == null) ? EMPTY : raw.trim();
    }

    /**
     * Renders the user's dialog bubble.
     *
     * @param text the text to display in the user bubble
     */
    private void renderUserBubble(String text) {
        dialogContainer.getChildren().add(DialogBox.getUserDialog(text, userImage));
    }

    /**
     * Renders Tim's dialog bubble, using an error style if the response begins with the error prefix.
     *
     * @param response the response text from the core logic; may be null
     */
    private void renderTimBubble(String response) {
        final boolean isError = response != null && response.startsWith(ERROR_PREFIX);
        dialogContainer.getChildren().add(isError
                ? DialogBox.getErrorDialog(response, timImage)
                : DialogBox.getTimDialog(response, timImage));
    }

    /**
     * Processes the exit flow if the input is a termination command.
     *
     * @param input the normalized user input
     */
    private void processExitIfNeeded(String input) {
        if (!BYE_COMMAND.equalsIgnoreCase(input)) {
            return;
        }
        PauseTransition delay = new PauseTransition(Duration.millis(EXIT_DELAY_MS));
        delay.setOnFinished(e -> {
            if (reminderService != null) {
                reminderService.stop();
            }
            Platform.exit();
        });
        delay.play();
    }
}
