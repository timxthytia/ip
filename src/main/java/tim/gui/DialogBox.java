package tim.gui;

import java.io.IOException;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * A reusable chat bubble control composed of a text label and a circular avatar image.
 * Supports variants for user, Tim, and error messages.
 */
public final class DialogBox extends HBox {
    private static final String FXML_PATH = "/view/DialogBox.fxml";
    private static final String CLASS_CHAT_TEXT = "chat-text";
    private static final String CLASS_USER_BUBBLE = "user-bubble";
    private static final String CLASS_TIM_BUBBLE = "tim-bubble";
    private static final String CLASS_ERROR_BUBBLE = "error-bubble";
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;
    @FXML
    private VBox messageContainer;
    /**
     * Constructs a DialogBox with the specified text and image.
     *
     * @param text the text to display in the dialog box
     * @param img the image to display as the avatar
     */
    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_PATH));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML for DialogBox from " + FXML_PATH, e);
        }

        // Normalize and apply content
        this.dialog.setText(text == null ? "" : text.trim());
        this.dialog.getStyleClass().add(CLASS_CHAT_TEXT);
        this.displayPicture.setImage(Objects.requireNonNull(img, "Display image must not be null."));

        // Create circular clip for profile picture without using bindings
        applyCircularClip(displayPicture);
    }

    /**
     * Applies a circular clip to the given ImageView using fixed values.
     *
     * @param iv the ImageView to apply the circular clip to
     */
    private static void applyCircularClip(ImageView iv) {
        Circle clip = new Circle();
        clip.setCenterX(49.5);
        clip.setCenterY(49.5);
        clip.setRadius(49.5);
        iv.setClip(clip);
    }

    /**
     * Flips the dialog so the avatar is on the left and the message is on the right.
     *
     */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        getChildren().setAll(displayPicture, messageContainer);
    }

    /**
     * Creates a dialog box for user input.
     *
     * @param text the text to display in the dialog box
     * @param img the image to display as the avatar
     * @return a DialogBox configured for user input
     */
    public static DialogBox getUserDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add(CLASS_USER_BUBBLE);
        return db;
    }

    /**
     * Creates a dialog box for Tim's responses.
     *
     * @param text the text to display in the dialog box
     * @param img the image to display as the avatar
     * @return a DialogBox configured for Tim's responses
     */
    public static DialogBox getTimDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add(CLASS_TIM_BUBBLE);
        db.flip();
        return db;
    }

    /**
     * Creates a dialog box for error messages with special red styling.
     *
     * @param text the text to display in the dialog box
     * @param img the image to display as the avatar
     * @return a DialogBox configured for error messages
     */
    public static DialogBox getErrorDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().addAll(CLASS_TIM_BUBBLE, CLASS_ERROR_BUBBLE);
        db.flip();
        return db;
    }
}
