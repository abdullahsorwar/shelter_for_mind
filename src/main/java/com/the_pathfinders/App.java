package com.the_pathfinders;

import com.the_pathfinders.db.DB;
import com.the_pathfinders.db.DbMigrations;
import com.the_pathfinders.util.PasswordResetServer;
import com.the_pathfinders.verification.VerificationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Show splash screen immediately
        SplashScreen splash = new SplashScreen();
        splash.show();
        
        // Initialize database in background thread to keep splash screen responsive
        Thread initThread = new Thread(() -> {
            try {
                System.out.println("Initializing database...");
                
                // Try to initialize database with retries for Neon wake-up
                int maxRetries = 3;
                int retryCount = 0;
                boolean dbInitialized = false;
                
                while (retryCount < maxRetries && !dbInitialized) {
                    try {
                        DB.init();
                        DbMigrations.runAll();
                        System.out.println("Database initialized successfully.");
                        dbInitialized = true;
                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("Database connection attempt " + retryCount + " failed: " + e.getMessage());
                        if (retryCount < maxRetries) {
                            System.out.println("Retrying in 5 seconds... (Neon database might be waking up)");
                            Thread.sleep(5000);
                        } else {
                            System.err.println("Failed to connect to database after " + maxRetries + " attempts.");
                            System.err.println("The app will continue but some features may not work.");
                            System.err.println("Please check your internet connection and Neon database status.");
                        }
                    }
                }

                // Start loading background music asynchronously (non-blocking)
                System.out.println("Starting background music load (async)...");
                try {
                    MusicManager.preloadBackgroundMusic(); // Loads in background thread, returns immediately
                } catch (Exception e) {
                    System.err.println("Could not start music load: " + e.getMessage());
                }

                // Load UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        System.out.println("Loading user interface...");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/initial.fxml"));
                        Parent root = loader.load();

                        Scene scene = new Scene(root, 1280, 720);
                        stage.setScene(scene);
                        stage.setTitle("Shelter for Mind");
                        
                        // Set app icons - window icon and taskbar icon
                        try {
                            // Window icon (title bar)
                            Image windowIcon = new Image(getClass().getResourceAsStream("/assets/images/shelter_for_mind.png"));
                            stage.getIcons().add(windowIcon);
                            
                            // Taskbar icon (add smaller size for better taskbar appearance)
                            Image taskbarIcon = new Image(getClass().getResourceAsStream("/assets/images/logo_taskbar.png"));
                            stage.getIcons().add(taskbarIcon);
                        } catch (Exception e) {
                            System.err.println("Could not load app icon: " + e.getMessage());
                        }
                        
                        stage.setMinWidth(1280);
                        stage.setMinHeight(720);
                        
                        System.out.println("Showing window...");
                        
                        // Close splash screen before showing main window
                        splash.close();
                        
                        // Manually calculate center position (same logic as splash screen)
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        double centerX = (screenBounds.getWidth() - 1280) / 2;
                        double centerY = (screenBounds.getHeight() - 720) / 2;
                        
                        stage.setX(centerX);
                        stage.setY(centerY);
                        
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
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Failed to load UI: " + e.getMessage());
                        splash.close();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> splash.close());
            }
        });
        
        initThread.setDaemon(false);
        initThread.start();
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