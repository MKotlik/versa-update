import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Created by mikek on 6/6/2016.
 */
public class MainApp extends Application {
    //Variables
    private Stage primaryStage;
    private AnchorPane rootLayout;

    //Start
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
