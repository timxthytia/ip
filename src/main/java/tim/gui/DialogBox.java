package tim.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * This control contains a dialog box consisting of an ImageView to represent the speaker's face
 * and a label containing text from the speaker.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

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
        displayPicture.getStyleClass().add("profile-image");
        displayPicture.setImage(img);

        // Create circular clip - this actually makes it circular
        Circle clip = new Circle();
        clip.setCenterX(49.5); // Half of 99px
        clip.setCenterY(49.5);
        clip.setRadius(49.5);
        displayPicture.setClip(clip);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        this.setAlignment(Pos.TOP_LEFT);
        var tmp = dialog.getText();
        dialog.setText(tmp);
        getChildren().clear();
        getChildren().addAll(displayPicture, dialog);
    }

    /**
     * Creates a dialog box for user input.
     *
     * @param text The text to display.
     * @param img The user's display picture.
     * @return A DialogBox for user input.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add("user-bubble");
        return db;
    }

    /**
     * Creates a dialog box for Tim's responses.
     *
     * @param text The text to display.
     * @param img Tim's display picture.
     * @return A DialogBox for Tim's response.
     */
    public static DialogBox getTimDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.getStyleClass().add("tim-bubble");
        db.flip();
        return db;
    }
}
