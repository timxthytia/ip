package tim.gui;

import java.io.IOException;

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
 * This control contains a dialog box consisting of an ImageView to represent the speaker's face
 * and a label containing text from the speaker wrapped in a chat bubble.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;
    @FXML
    private VBox messageContainer;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/DialogBox.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dialog.setText(text);
        dialog.getStyleClass().add("chat-text");
        displayPicture.setImage(img);

        // Create circular view for profile picture
        Circle clip = new Circle();
        clip.setCenterX(49.5);
        clip.setCenterY(49.5);
        clip.setRadius(49.5);
        displayPicture.setClip(clip);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        this.setAlignment(Pos.TOP_LEFT);
        getChildren().clear();
        getChildren().addAll(displayPicture, messageContainer);
    }

    /**
     * Creates a dialog box for user input.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add("user-bubble");
        return db;
    }

    /**
     * Creates a dialog box for Tim's responses.
     */
    public static DialogBox getTimDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add("tim-bubble");
        db.flip();
        return db;
    }

    /**
     * Creates a dialog box for error messages with special red styling.
     */
    public static DialogBox getErrorDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().addAll("tim-bubble", "error-bubble");
        db.flip();
        return db;
    }
}
