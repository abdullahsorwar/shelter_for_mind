package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminLoginController implements Initializable {

    @FXML private Pane root;
    @FXML private Pane backgroundPane;
    @FXML private Rectangle gradientRect;
    @FXML private Pane contentPane;
    @FXML private Pane mainCard;
    
    // Login elements
    @FXML private Text loginTitle;
    @FXML private TextField keeperIdField;
    @FXML private PasswordField keeperPasswordField;
    @FXML private TextField keeperPasswordVisibleField;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink createAccountLink;
    
    // Theme toggle
    @FXML private Button themeToggleButton;
    private boolean isLightMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Apply CSS
        root.getStylesheets().add(getClass().getResource("/com/the_pathfinders/css/admin_login.css").toExternalForm());
        
        // Set initial prompt text colors (dark mode - grayish white)
        setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
        
        // After scene is set, bind to window size
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.prefWidthProperty().bind(newScene.widthProperty());
                root.prefHeightProperty().bind(newScene.heightProperty());
                backgroundPane.prefWidthProperty().bind(newScene.widthProperty());
                backgroundPane.prefHeightProperty().bind(newScene.heightProperty());
                gradientRect.widthProperty().bind(newScene.widthProperty());
                gradientRect.heightProperty().bind(newScene.heightProperty());
                
                // Center content
                centerContent();
                newScene.widthProperty().addListener((o, ov, nv) -> centerContent());
                newScene.heightProperty().addListener((o, ov, nv) -> centerContent());
            }
        });
        
        // Add hover effect to login button
        addLoginButtonHoverEffect();
        
        // Bind theme toggle button to window top-right corner
        bindThemeButtonToTopRight();
        
        // Add entrance animation
        playEntranceAnimation();
    }
    
    private void bindThemeButtonToTopRight() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Position button 15px from right edge and 20px from top
                themeToggleButton.layoutXProperty().bind(
                    newScene.widthProperty().subtract(themeToggleButton.widthProperty()).subtract(15)
                );
            }
        });
    }
    
    private void centerContent() {
        if (root.getScene() != null) {
            double sceneWidth = root.getScene().getWidth();
            double sceneHeight = root.getScene().getHeight();
            double contentWidth = contentPane.getPrefWidth();
            double contentHeight = contentPane.getPrefHeight();
            
            contentPane.setLayoutX((sceneWidth - contentWidth) / 2);
            contentPane.setLayoutY((sceneHeight - contentHeight) / 2);
        }
    }
    
    private void playEntranceAnimation() {
        // Start with card scaled down and invisible
        mainCard.setOpacity(0);
        mainCard.setScaleX(0.8);
        mainCard.setScaleY(0.8);
        
        // Fade in and scale up
        FadeTransition fade = new FadeTransition(Duration.millis(500), mainCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), mainCard);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition entrance = new ParallelTransition(fade, scale);
        entrance.setDelay(Duration.millis(200));
        entrance.play();
    }
    
    private void addLoginButtonHoverEffect() {
        if (loginButton == null) return;
        
        // Use Platform.runLater to ensure button is fully rendered
        javafx.application.Platform.runLater(() -> {
            loginButton.setOnMouseEntered(e -> {
                // On hover: text becomes bigger
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
                scale.setToX(1.08);
                scale.setToY(1.08);
                scale.play();
            });
            
            loginButton.setOnMouseExited(e -> {
                // On exit: returns to normal
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        });
    }
    
    @FXML
    private void handleLogin() {
        String keeperId = keeperIdField.getText().trim();
        String keeperPass = keeperPasswordField.getText();
        
        if (keeperId.isEmpty() || keeperPass.isEmpty()) {
            showAlert("Login Error", "Please fill in all fields");
            return;
        }
        
        // TODO: Implement actual admin authentication logic
        System.out.println("Keeper login attempt: " + keeperId);
        
        // For now, just show success message
        showAlert("Login", "Keeper authentication to be implemented");
    }
    
    @FXML
    private void handleShowPassword() {
        if (showPasswordCheckbox.isSelected()) {
            // Show password: copy password to visible field and switch visibility
            keeperPasswordVisibleField.setText(keeperPasswordField.getText());
            keeperPasswordField.setVisible(false);
            keeperPasswordVisibleField.setVisible(true);
            
            // Apply current theme's prompt text color
            if (isLightMode) {
                setPromptTextColors(Color.rgb(102, 102, 102));
            } else {
                setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
            }
            
            // Sync text changes from visible field to password field
            keeperPasswordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
                keeperPasswordField.setText(newVal);
            });
        } else {
            // Hide password: copy visible text back and switch visibility
            keeperPasswordField.setText(keeperPasswordVisibleField.getText());
            keeperPasswordVisibleField.setVisible(false);
            keeperPasswordField.setVisible(true);
            
            // Reapply current theme's prompt text color
            if (isLightMode) {
                setPromptTextColors(Color.rgb(102, 102, 102));
            } else {
                setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
            }
        }
    }
    
    @FXML
    private void handleForgotPassword() {
        showAlert("Forgot Password", "Password recovery functionality to be implemented");
    }
    
    @FXML
    private void handleCreateAccount() {
        showAlert("Create Account", "Account creation form to be implemented");
    }
    
    @FXML
    private void handleThemeToggle() {
        isLightMode = !isLightMode;
        
        // Fade out, change theme, fade back in for smooth transition (400ms total)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.95);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
        fadeIn.setFromValue(0.95);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> {
            if (isLightMode) {
                // Switch to light mode
                root.getStyleClass().add("light-mode");
                themeToggleButton.setText("🌙"); // Moon icon for dark mode option
                // Set prompt text colors to dark gray for light mode
                setPromptTextColors(Color.rgb(102, 102, 102)); // #666666
            } else {
                // Switch to dark mode
                root.getStyleClass().remove("light-mode");
                themeToggleButton.setText("☀"); // Sun icon for light mode option
                // Set prompt text colors to light gray for dark mode
                setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
            }
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void setPromptTextColors(Color color) {
        // Set prompt text fill for all text input fields
        String colorStyle = String.format("-fx-prompt-text-fill: rgba(%d, %d, %d, %.2f);",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255),
            color.getOpacity());
        
        // Clear and set style to ensure it takes effect
        keeperIdField.setStyle(colorStyle);
        keeperPasswordField.setStyle(colorStyle);
        keeperPasswordVisibleField.setStyle(colorStyle);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
