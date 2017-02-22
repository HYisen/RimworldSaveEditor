package net.alexhyisen.rse.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        controller=loader.getController();
        primaryStage.setTitle("RimworldSaveEditor");
        primaryStage.setScene(new Scene(root, 960, 540));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        //System.out.println("END");
        controller.handleCloseEvent();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
