package com.the_pathfinders;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.application.Platform;

import com.the_pathfinders.db.KeeperRepository;
import com.the_pathfinders.util.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ResourceBundle;

public class PasswordResetController implements Initializable {

    @FXML private Pane root;
    @FXML private Pane backgroundPane;
    @FXML private Rectangle gradientRect;
    @FXML private Pane contentPane;
    @FXML private Pane mainCard;
    
    @FXML private Text resetTitle;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisibleField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private Button resetButton;
    @FXML private Hyperlink backToLoginLink;
    @FXML private Button themeToggleButton;
    
    private String resetToken;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Sync visible/masked password fields
        newPasswordField.textProperty().bindBidirectional(newPasswordVisibleField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordVisibleField.textProperty());
        
        // Apply current theme
        ThemeManager.applyTheme(root);
        
        // Setup animations
        setupAnimations();
        
        // Setup responsive layout when scene is available
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Bind sizes to scene
                root.prefWidthProperty().bind(newScene.widthProperty());
                root.prefHeightProperty().bind(newScene.heightProperty());
                backgroundPane.prefWidthProperty().bind(newScene.widthProperty());
                backgroundPane.prefHeightProperty().bind(newScene.heightProperty());
                gradientRect.widthProperty().bind(newScene.widthProperty());
                gradientRect.heightProperty().bind(newScene.heightProperty());
                
                // Center content on resize
                newScene.widthProperty().addListener((o, ov, nv) -> centerContent());
                newScene.heightProperty().addListener((o, ov, nv) -> centerContent());
                
                // Initial centering
                centerContent();
                
                // Center title text
                centerTitle();
                
                // Bind theme toggle button position
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
    
    private void centerTitle() {
        Platform.runLater(() -> {
            double cardWidth = mainCard.getPrefWidth();
            double titleWidth = resetTitle.getLayoutBounds().getWidth();
            resetTitle.setLayoutX((cardWidth - titleWidth) / 2);
        });
    }
    
    public void setResetToken(String token) {
        this.resetToken = token;
        
        // Validate token immediately
        try {
            KeeperRepository.PasswordResetToken tokenInfo = KeeperRepository.validateResetToken(token);
            if (tokenInfo == null) {
                showAlert("Error", "Invalid or expired password reset link.\nPlease request a new password reset.");
                handleBackToLogin();
                return;
            }
            // Token is valid, keeper can proceed
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to validate reset token: " + e.getMessage());
            handleBackToLogin();
        }
    }
    
    @FXML
    private void handleShowPassword() {
        boolean showPassword = showPasswordCheckbox.isSelected();
        
        newPasswordField.setVisible(!showPassword);
        newPasswordVisibleField.setVisible(showPassword);
        
        confirmPasswordField.setVisible(!showPassword);
        confirmPasswordVisibleField.setVisible(showPassword);
    }
    
    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }
        
        if (newPassword.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long");
            return;
        }
        
        try {
            // Hash the new password
            String hashedPassword = hashPassword(newPassword);
            
            // Reset the password
            boolean success = KeeperRepository.resetPassword(resetToken, hashedPassword);
            
            if (success) {
                showAlert("Success", "Password has been reset successfully!\nYou can now log in with your new password.");
                handleBackToLogin();
            } else {
                showAlert("Error", "Failed to reset password. The reset link may have expired.\nPlease request a new password reset.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to reset password: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/admin_login.fxml"));
            Parent adminLoginRoot = loader.load();
            
            Scene scene = root.getScene();
            scene.setRoot(adminLoginRoot);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load login page");
        }
    }
    
    @FXML
    private void handleThemeToggle() {
        ThemeManager.toggleTheme();
        ThemeManager.applyTheme(root);
    }
    
    private void setupAnimations() {
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), contentPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        // Slide up animation
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.6), contentPane);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.play();
    }
    
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply theme to alert
        DialogPane dialogPane = alert.getDialogPane();
        if (ThemeManager.isLightMode()) {
            dialogPane.setStyle("-fx-background-color: #F5F7FA;");
        } else {
            dialogPane.setStyle("-fx-background-color: #1E1E1E;");
        }
        
        alert.showAndWait();
    }
}
