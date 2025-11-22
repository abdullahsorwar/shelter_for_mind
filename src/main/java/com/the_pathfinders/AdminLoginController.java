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
    @FXML private Pane emailFieldPane;
    @FXML private TextField emailField;
    @FXML private TextField keeperIdField;
    @FXML private PasswordField keeperPasswordField;
    @FXML private TextField keeperPasswordVisibleField;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private Button loginButton;
    @FXML private javafx.scene.layout.HBox linksContainer;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink createAccountLink;
    @FXML private javafx.scene.layout.HBox loginModeLink;
    @FXML private Hyperlink backToLoginLink;
    @FXML private javafx.scene.layout.VBox centerContent;
    
    // Theme toggle
    @FXML private Button themeToggleButton;
    private boolean isLightMode = false;
    private boolean isSignupMode = false;

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
        if (isSignupMode) {
            handleSignup();
        } else {
            performLogin();
        }
    }
    
    private void performLogin() {
        String keeperId = keeperIdField.getText().trim().toLowerCase();
        String keeperPass = keeperPasswordField.getText();
        
        if (keeperId.isEmpty() || keeperPass.isEmpty()) {
            showAlert("Login Error", "Please fill in all fields");
            return;
        }
        
        // Disable button to prevent double-clicks
        loginButton.setDisable(true);
        
        // Perform authentication in background thread
        new Thread(() -> {
            try {
                // First check if keeper is approved
                if (!com.the_pathfinders.db.KeeperRepository.isKeeperApproved(keeperId)) {
                    // Check if there's a pending signup
                    com.the_pathfinders.db.KeeperRepository.KeeperSignupRequest request = 
                        com.the_pathfinders.db.KeeperRepository.getSignupRequest(keeperId);
                    
                    javafx.application.Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        if (request == null) {
                            showAlert("Account Not Found", "No keeper account found with this ID.\n\nPlease sign up first.");
                        } else if (!request.emailVerified) {
                            showAlert("Email Not Verified", "Please verify your email address first.\n\nCheck your inbox for the verification link.");
                        } else if (request.status == com.the_pathfinders.db.KeeperRepository.KeeperStatus.PENDING) {
                            showAlert("Approval Pending", "Your account is pending approval by existing keepers.\n\nYou will receive an email once approved.");
                        } else if (request.status == com.the_pathfinders.db.KeeperRepository.KeeperStatus.REJECTED) {
                            showAlert("Account Rejected", "Your keeper account request was not approved.\n\nPlease contact support for more information.");
                        }
                    });
                    return;
                }
                
                // Authenticate
                boolean authenticated = com.the_pathfinders.db.KeeperRepository.authenticateKeeper(keeperId, keeperPass);
                
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    if (authenticated) {
                        showAlert("Login Successful", "Welcome, keeper " + keeperId + "!\n\nKeeper dashboard to be implemented.");
                        // TODO: Navigate to keeper dashboard
                    } else {
                        showAlert("Login Failed", "Invalid keeper ID or password.");
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Login error: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showAlert("Login Error", "An error occurred during login: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void handleSignup() {
        String email = emailField.getText().trim().toLowerCase();
        String keeperId = keeperIdField.getText().trim().toLowerCase();
        String keeperPass = keeperPasswordField.getText();
        
        if (email.isEmpty() || keeperId.isEmpty() || keeperPass.isEmpty()) {
            showAlert("Signup Error", "Please fill in all fields");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address");
            return;
        }
        
        // Validate keeper_id format (alphanumeric, underscore, 3-20 chars)
        if (!keeperId.matches("^[a-z0-9_]{3,20}$")) {
            showAlert("Invalid Keeper ID", "Keeper ID must be 3-20 characters (lowercase letters, numbers, underscore only)");
            return;
        }
        
        // Validate password strength (at least 8 characters)
        if (keeperPass.length() < 8) {
            showAlert("Weak Password", "Password must be at least 8 characters long");
            return;
        }
        
        // Disable button to prevent double-clicks
        loginButton.setDisable(true);
        
        // Perform signup in background thread
        new Thread(() -> {
            try {
                // Check if keeper_id already exists
                if (com.the_pathfinders.db.KeeperRepository.isKeeperIdExists(keeperId)) {
                    javafx.application.Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        showAlert("Keeper ID Taken", "This keeper ID is already registered. Please choose a different one.");
                    });
                    return;
                }
                
                // Check if email already exists
                if (com.the_pathfinders.db.KeeperRepository.isEmailExists(email)) {
                    javafx.application.Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        showAlert("Email Already Registered", "This email is already associated with another account.");
                    });
                    return;
                }
                
                // Hash password
                String passwordHash = com.the_pathfinders.db.KeeperRepository.hashPassword(keeperPass);
                
                // Create signup request
                com.the_pathfinders.db.KeeperRepository.createSignupRequest(keeperId, email, passwordHash);
                System.out.println("Keeper signup request created: " + keeperId);
                
                // Send verification email
                com.the_pathfinders.verification.VerificationManager verificationManager = 
                    com.the_pathfinders.verification.VerificationManager.getInstance();
                
                if (!verificationManager.isRunning()) {
                    verificationManager.start();
                }
                
                // Register keeper verification with server
                String verifyToken = com.the_pathfinders.verification.EmailService.generateVerificationToken(keeperId);
                // Use reflection to access HTTP server and register
                try {
                    java.lang.reflect.Field httpServerField = verificationManager.getClass().getDeclaredField("httpServer");
                    httpServerField.setAccessible(true);
                    com.the_pathfinders.verification.VerificationServer httpServer = 
                        (com.the_pathfinders.verification.VerificationServer) httpServerField.get(verificationManager);
                    httpServer.registerVerification(verifyToken, keeperId);
                    
                    // Send keeper verification email
                    com.the_pathfinders.verification.EmailService.sendKeeperVerificationEmail(email, keeperId, verifyToken);
                } catch (Exception e) {
                    System.err.println("Failed to send keeper verification email: " + e.getMessage());
                    e.printStackTrace();
                }
                
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showAlert("Verification Email Sent", 
                        "A verification email has been sent to " + email + ".\n\n" +
                        "Please check your inbox and click the verification link.\n\n" +
                        "After verification, existing keepers will review your request and " +
                        "you will receive a follow-up email once approved.");
                    
                    // Clear fields and switch back to login mode
                    emailField.clear();
                    keeperIdField.clear();
                    keeperPasswordField.clear();
                    keeperPasswordVisibleField.clear();
                    switchToLoginMode();
                });
                
            } catch (Exception e) {
                System.err.println("Keeper signup error: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showAlert("Signup Error", "An error occurred during signup: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation regex
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
        switchToSignupMode();
    }
    
    @FXML
    private void handleBackToLogin() {
        switchToLoginMode();
    }
    
    private void switchToSignupMode() {
        isSignupMode = true;
        
        // Fade out inner content
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), centerContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            // Update UI elements
            loginTitle.setText("Create Account");
            loginButton.setText("SIGN UP");
            
            // Center the title text
            centerTitle();
            
            // Show email field
            emailFieldPane.setVisible(true);
            emailFieldPane.setManaged(true);
            
            // Update links
            forgotPasswordLink.setVisible(false);
            forgotPasswordLink.setManaged(false);
            createAccountLink.setVisible(false);
            createAccountLink.setManaged(false);
            loginModeLink.setVisible(true);
            loginModeLink.setManaged(true);
            
            // Reapply prompt text colors
            if (isLightMode) {
                setPromptTextColors(Color.rgb(102, 102, 102));
            } else {
                setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
            }
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), centerContent);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void switchToLoginMode() {
        isSignupMode = false;
        
        // Fade out inner content
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), centerContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            // Update UI elements
            loginTitle.setText("Welcome, Keeper!");
            loginButton.setText("LOGIN");
            
            // Center the title text
            centerTitle();
            
            // Hide email field
            emailFieldPane.setVisible(false);
            emailFieldPane.setManaged(false);
            
            // Clear email field
            emailField.clear();
            
            // Update links
            forgotPasswordLink.setVisible(true);
            forgotPasswordLink.setManaged(true);
            createAccountLink.setVisible(true);
            createAccountLink.setManaged(true);
            loginModeLink.setVisible(false);
            loginModeLink.setManaged(false);
            
            // Reapply prompt text colors
            if (isLightMode) {
                setPromptTextColors(Color.rgb(102, 102, 102));
            } else {
                setPromptTextColors(Color.rgb(255, 255, 255, 0.6));
            }
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), centerContent);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
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
    
    private void centerTitle() {
        // Center the title text based on its actual width
        javafx.application.Platform.runLater(() -> {
            double titleWidth = loginTitle.getLayoutBounds().getWidth();
            double cardWidth = mainCard.getPrefWidth();
            loginTitle.setLayoutX((cardWidth - titleWidth) / 2);
        });
    }
    
    private void setPromptTextColors(Color color) {
        // Set prompt text fill for all text input fields
        String colorStyle = String.format("-fx-prompt-text-fill: rgba(%d, %d, %d, %.2f);",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255),
            color.getOpacity());
        
        // Clear and set style to ensure it takes effect
        if (emailField != null) emailField.setStyle(colorStyle);
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
