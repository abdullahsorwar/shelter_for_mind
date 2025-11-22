package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import com.the_pathfinders.util.PasswordResetServer;
import com.the_pathfinders.verification.VerificationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
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

        // Start password reset server
        System.out.println("Starting password reset server...");
        PasswordResetServer.start();

        stage.setOnCloseRequest(e -> {
            DB.shutdown();
            MusicManager.stopBackgroundMusic();
            // Stop verification servers
            VerificationManager.getInstance().stop();
            // Stop password reset server
            PasswordResetServer.stop();
        });
    }
    
    public static void showPasswordResetPage(String token) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/the_pathfinders/fxml/password_reset.fxml"));
            Parent root = loader.load();
            
            PasswordResetController controller = loader.getController();
            controller.setResetToken(token);
            
            Scene scene = primaryStage.getScene();
            scene.setRoot(root);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load password reset page: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}