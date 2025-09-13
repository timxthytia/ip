package tim.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tim.app.Tim;

/** JavaFX Application that loads MainWindow.fxml. */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        AnchorPane root = loader.load();

        MainWindow controller = loader.getController();
        controller.setTim(new Tim("data/tasks.txt")); // reuse save file

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Main.class.getResource("/css/app.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setTitle("Tim");
        stage.setResizable(false);
        stage.show();
    }
}
