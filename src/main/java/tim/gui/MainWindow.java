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


/** Controller for MainWindow.fxml. Handles initialization and user interactions. */
public class MainWindow extends AnchorPane {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox dialogContainer;
    @FXML private TextField userInput;
    @FXML private Button sendButton;

    private Tim tim;
    private Image userImage = new Image(getClass().getResourceAsStream("/images/DaUser.png"));
    private Image timImage = new Image(getClass().getResourceAsStream("/images/DaDuke.png"));

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
    }

    /**
     * Injects the Tim instance to be used by the controller.
     * @param tim Tim instance to handle user input and generate responses.
     */
    public void setTim(Tim tim) {
        this.tim = tim;
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
            delay.setOnFinished(e -> Platform.exit());
            delay.play();
        }
    }
}
