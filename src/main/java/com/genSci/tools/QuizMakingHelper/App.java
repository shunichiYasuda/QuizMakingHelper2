package com.genSci.tools.QuizMakingHelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("primary.fxml"));
    	scene = new Scene(root);
    	stage.setTitle("Moodle穴埋め問題作成支援 ver.1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}