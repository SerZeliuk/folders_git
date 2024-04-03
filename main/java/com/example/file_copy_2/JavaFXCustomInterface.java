package com.example.file_copy_2;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.util.Objects;

public class JavaFXCustomInterface extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FolderDatabaseCreator.connect("baza_twoja_stara.db");

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainLayout.fxml")));

        primaryStage.setTitle("Custom Interface with Integrated Video Player");
        Scene scene = new Scene(root, 1100, 800);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
