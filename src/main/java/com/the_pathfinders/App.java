package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DB.init();
        DbMigrations.runAll();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/initial.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Shelter of Mind");
        
        // Set app icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load app icon: " + e.getMessage());
        }
        
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.show();

        stage.setOnCloseRequest(e -> DB.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}