package org.sudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.sudoku.controller.*;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private final MainController mainController;

    public App() {
        this.mainController = new MainController();
    }

    @Override
    public void stop() throws Exception {
        this.mainController.stop();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Sudoku");
        stage.setResizable(false);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/main.fxml"));
        loader.setController(mainController);

        Parent root = loader.load();
        mainController.setDefaults();

        var scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}