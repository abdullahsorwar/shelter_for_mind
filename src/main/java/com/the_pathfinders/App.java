package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import com.the_pathfinders.verification.VerificationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Initializing database...");
        DB.init();
        DbMigrations.runAll();
        System.out.println("Database initialized successfully.");

        // Start loading background music asynchronously (non-blocking)
        System.out.println("Starting background music load (async)...");
        try {
            MusicManager.preloadBackgroundMusic(); // Loads in background thread, returns immediately
        } catch (Exception e) {
            System.err.println("Could not start music load: " + e.getMessage());
        }

        System.out.println("Loading user interface...");
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
        
        System.out.println("Showing window...");
        stage.show();
        
        // Request music playback (will play when ready if still loading)
        System.out.println("Requesting background music playback...");
        try {
            MusicManager.playBackgroundMusic();
        } catch (Exception e) {
            System.err.println("Could not play background music: " + e.getMessage());
        }

        stage.setOnCloseRequest(e -> {
            DB.shutdown();
            MusicManager.stopBackgroundMusic();
            // Stop verification servers
            VerificationManager.getInstance().stop();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}