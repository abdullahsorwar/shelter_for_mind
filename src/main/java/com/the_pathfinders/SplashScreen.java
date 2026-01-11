package com.the_pathfinders;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen that displays during application initialization and database connection.
 * Shows the app logo and loading message.
 */
public class SplashScreen {
    private Stage splashStage;
    private boolean closed = false;

    /**
     * Show the splash screen
     */
    public void show() {
        Platform.runLater(() -> {
            try {
                splashStage = new Stage();
                splashStage.initStyle(StageStyle.UNDECORATED); // No window decorations
                
                // Set app icons for splash screen (taskbar icon)
                try {
                    Image windowIcon = new Image(getClass().getResourceAsStream("/assets/images/shelter_for_mind.png"));
                    splashStage.getIcons().add(windowIcon);
                    Image taskbarIcon = new Image(getClass().getResourceAsStream("/assets/images/logo_taskbar.png"));
                    splashStage.getIcons().add(taskbarIcon);
                } catch (Exception e) {
                    System.err.println("Could not load splash screen icon: " + e.getMessage());
                }
                
                // Load the app icon
                Image logo = new Image(getClass().getResourceAsStream("/assets/images/app-icon.png"));
                ImageView logoView = new ImageView(logo);
                
                // Set logo size (adjust as needed)
                logoView.setFitWidth(150);
                logoView.setFitHeight(150);
                logoView.setPreserveRatio(true);
                
                // Create loading text
                Text loadingText = new Text("Loading Shelter for Mind...");
                loadingText.setFill(Color.BLACK);
                loadingText.setFont(Font.font("System", 16));
                
                // Create layout
                VBox root = new VBox(20); // 20px spacing between logo and text
                root.setAlignment(Pos.CENTER);
                root.setStyle("-fx-background-color: white;"); // White background for text visibility
                root.getChildren().addAll(logoView, loadingText);
                
                // Create scene
                Scene scene = new Scene(root, 300, 250);
                
                splashStage.setScene(scene);
                
                // Manually calculate center position
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                double centerX = (screenBounds.getWidth() - 300) / 2;
                double centerY = (screenBounds.getHeight() - 250) / 2;
                
                splashStage.setX(centerX);
                splashStage.setY(centerY);
                
                splashStage.show();
                
            } catch (Exception e) {
                System.err.println("Failed to show splash screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Close the splash screen
     */
    public void close() {
        if (closed) return;
        closed = true;
        
        Platform.runLater(() -> {
            if (splashStage != null) {
                splashStage.close();
            }
        });
    }
}
