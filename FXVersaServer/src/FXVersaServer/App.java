/**
 * VersaCheckers
 * FXVersaServer - App
 * @author Mikhail Kotlik, Sam Xu
 * @version 1
 * Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 */

package FXVersaServer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.border.Border;


public class App extends Application {
    //Variables
    private Stage primaryStage;
    private AnchorPane rootLayout;

    //Start
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("VersaCheckers Server");

        try {
            // Load the root layout from the fxml file
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/RootLayout.fxml"));
            rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            System.err.println("SERVER: JavaFX RootLayout for server could not be loaded.");
            e.printStackTrace();
        }

        showUserGameList();

    }

    /**
     * Returns the main stage.
     * @return main stage of the VersaServer app
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Displays the user/game list in the window
     */
    public void showUserGameList() {
        try {
            // Load the fxml file and set into the center of the main layout
            FXMLLoader loader1 = new FXMLLoader(App.class.getResource("view/UserList.fxml"));
            FXMLLoader loader2 = new FXMLLoader(App.class.getResource("view/GameList.fxml"));
            AnchorPane userList = (AnchorPane) loader1.load();
            AnchorPane gameList = (AnchorPane) loader2.load();
            TabPane userGamePane = ((TabPane) ((VBox) ((SplitPane) ((TabPane) ((BorderPane) rootLayout.getChildren().
                    get(0)).getChildren().get(1)).getTabs().get(0).getContent()).getItems().get(0)).getChildren().
                    get(1));
            userGamePane.getTabs().get(0).setContent(userList);
            userGamePane.getTabs().get(1).setContent(gameList);
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
