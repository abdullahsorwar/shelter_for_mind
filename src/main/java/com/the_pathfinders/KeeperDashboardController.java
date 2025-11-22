package com.the_pathfinders;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import com.the_pathfinders.db.KeeperRepository;
import com.the_pathfinders.verification.EmailService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class KeeperDashboardController implements Initializable {

    @FXML private Pane root;
    @FXML private Pane backgroundPane;
    @FXML private Rectangle gradientRect;
    @FXML private HBox mainContainer;
    @FXML private VBox leftPanel;
    @FXML private VBox rightPanel;
    
    // Left Panel
    @FXML private VBox navigationMenu;
    @FXML private Button keeperVerificationBtn;
    @FXML private Button soulModerationBtn;
    @FXML private Button journalModerationBtn;
    @FXML private Button appointmentBtn;
    @FXML private Button achievementBtn;
    @FXML private ImageView keeperProfileImage;
    @FXML private Label keeperNameLabel;
    @FXML private Label keeperIdLabel;
    @FXML private Button logoutBtn;
    
    // Right Panel
    @FXML private Label contentTitle;
    @FXML private Button themeToggleButton;
    @FXML private StackPane contentBody;
    @FXML private ScrollPane keeperVerificationPane;
    @FXML private VBox pendingSignupsContainer;
    @FXML private VBox emptyState;
    @FXML private Button refreshBtn;
    @FXML private Label refreshLoadingIcon;
    
    // Soul Moderation Section
    @FXML private ScrollPane soulModerationPane;
    @FXML private VBox soulsListContainer;
    @FXML private VBox soulsEmptyState;
    @FXML private Label activeSoulsCount;
    @FXML private Label inactiveSoulsCount;
    @FXML private Label totalSoulsCount;
    @FXML private Button refreshSoulsBtn;
    
    @FXML private VBox otherSectionPane;
    @FXML private Label placeholderText;
    
    private String currentKeeperId = "the_pathfinders"; // Will be set from login
    private Button activeNavButton = null;
    
    public void setKeeperInfo(String keeperId) {
        this.currentKeeperId = keeperId;
        keeperIdLabel.setText(keeperId);
        
        // Load keeper profile to get short name synchronously
        try {
            KeeperRepository.KeeperProfile profile = KeeperRepository.getKeeperProfile(keeperId);
            
            // Set name: use short name if available, otherwise just "Keeper"
            if (profile.shortName != null && !profile.shortName.trim().isEmpty()) {
                keeperNameLabel.setText(profile.shortName);
            } else {
                keeperNameLabel.setText("Keeper");
            }
            
            // Load profile image
            loadKeeperProfileImage(keeperId);
        } catch (Exception e) {
            System.err.println("Failed to load keeper profile: " + e.getMessage());
            keeperNameLabel.setText("Keeper");
            loadKeeperProfileImage(keeperId);
        }
    }
    
    private void loadKeeperProfileImage(String keeperId) {
        try {
            // Try to load from assets/keeper_img/<keeper_id>.jpg
            String imagePath = "/assets/keeper_img/" + keeperId + ".jpg";
            URL imageUrl = getClass().getResource(imagePath);
            
            if (imageUrl != null) {
                javafx.scene.image.Image profileImage = new javafx.scene.image.Image(imageUrl.toExternalForm());
                keeperProfileImage.setImage(profileImage);
                System.out.println("Loaded keeper profile image: " + imagePath);
            } else {
                // Fallback to default username icon
                URL defaultUrl = getClass().getResource("/assets/icons/username.png");
                if (defaultUrl != null) {
                    javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(defaultUrl.toExternalForm());
                    keeperProfileImage.setImage(defaultImage);
                }
                System.out.println("Keeper profile image not found, using default icon");
            }
        } catch (Exception e) {
            System.err.println("Failed to load keeper profile image: " + e.getMessage());
            // Use default icon on error
            try {
                URL defaultUrl = getClass().getResource("/assets/icons/username.png");
                if (defaultUrl != null) {
                    javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(defaultUrl.toExternalForm());
                    keeperProfileImage.setImage(defaultImage);
                }
            } catch (Exception ex) {
                System.err.println("Failed to load default icon: " + ex.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Apply CSS
        root.getStylesheets().add(getClass().getResource("/com/the_pathfinders/css/keeper_dashboard.css").toExternalForm());
        
        // Apply current theme from ThemeManager
        com.the_pathfinders.util.ThemeManager.applyTheme(root);
        if (themeToggleButton != null) {
            themeToggleButton.setText(com.the_pathfinders.util.ThemeManager.isLightMode() ? "🌙" : "☀");
        }
        
        // Add circular clip to profile image
        Circle clip = new Circle(25, 25, 25);
        keeperProfileImage.setClip(clip);
        
        // Bind to window size
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.prefWidthProperty().bind(newScene.widthProperty());
                root.prefHeightProperty().bind(newScene.heightProperty());
                backgroundPane.prefWidthProperty().bind(newScene.widthProperty());
                backgroundPane.prefHeightProperty().bind(newScene.heightProperty());
                gradientRect.widthProperty().bind(newScene.widthProperty());
                gradientRect.heightProperty().bind(newScene.heightProperty());
                
                // Make main container responsive
                newScene.widthProperty().addListener((o, ov, nv) -> updateResponsiveLayout(nv.doubleValue()));
                newScene.heightProperty().addListener((o, ov, nv) -> updateContainerHeight(nv.doubleValue()));
                
                // Initial layout update
                updateResponsiveLayout(newScene.getWidth());
                updateContainerHeight(newScene.getHeight());
            }
        });
        
        // Set initial active button
        setActiveNavButton(keeperVerificationBtn);
        
        // Load pending signups
        loadPendingSignups();
        
        // Add entrance animation
        playEntranceAnimation();
    }
    
    private void playEntranceAnimation() {
        // Start with content scaled down and invisible
        contentBody.setOpacity(0);
        contentBody.setScaleX(0.95);
        contentBody.setScaleY(0.95);
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), contentBody);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), contentBody);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition entrance = new ParallelTransition(fade, scale);
        entrance.setDelay(Duration.millis(150));
        entrance.play();
    }
    
    private void updateResponsiveLayout(double windowWidth) {
        if (mainContainer != null) {
            double containerWidth = windowWidth - 40; // 20px margin on each side
            mainContainer.setPrefWidth(containerWidth);
            
            // Adjust panel sizes based on window width
            if (windowWidth < 1000) {
                leftPanel.setPrefWidth(200);
            } else if (windowWidth < 1200) {
                leftPanel.setPrefWidth(240);
            } else {
                leftPanel.setPrefWidth(260);
            }
            
            // Right panel takes remaining space
            double rightPanelWidth = containerWidth - leftPanel.getPrefWidth() - 20; // 20px spacing
            rightPanel.setPrefWidth(rightPanelWidth);
        }
    }
    
    private void updateContainerHeight(double windowHeight) {
        if (mainContainer != null) {
            double containerHeight = windowHeight - 110; // 90px from top + 20px margin
            mainContainer.setPrefHeight(containerHeight);
            leftPanel.setPrefHeight(containerHeight);
            rightPanel.setPrefHeight(containerHeight);
        }
    }
    
    private void setActiveNavButton(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeNavButton = button;
    }
    
    @FXML
    private void showKeeperVerification() {
        switchContent("Keeper Verification", "verification");
        setActiveNavButton(keeperVerificationBtn);
        loadPendingSignups();
    }
    
    @FXML
    private void showSoulModeration() {
        switchContent("Soul Moderation", "souls");
        setActiveNavButton(soulModerationBtn);
        loadSoulModeration();
    }
    
    @FXML
    private void showJournalModeration() {
        switchContent("Journal Moderation", "other");
        setActiveNavButton(journalModerationBtn);
        placeholderText.setText("Journal Moderation - Coming Soon");
    }
    
    @FXML
    private void showAppointment() {
        switchContent("Appointment", "other");
        setActiveNavButton(appointmentBtn);
        placeholderText.setText("Appointment - Coming Soon");
    }
    
    @FXML
    private void showAchievement() {
        switchContent("Achievement", "other");
        setActiveNavButton(achievementBtn);
        placeholderText.setText("Achievement - Coming Soon");
    }
    
    private void switchContent(String title, String paneToShow) {
        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentBody);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            contentTitle.setText(title);
            
            // Hide all panes
            keeperVerificationPane.setVisible(false);
            keeperVerificationPane.setManaged(false);
            soulModerationPane.setVisible(false);
            soulModerationPane.setManaged(false);
            otherSectionPane.setVisible(false);
            otherSectionPane.setManaged(false);
            
            // Show requested pane
            switch (paneToShow) {
                case "verification" -> {
                    keeperVerificationPane.setVisible(true);
                    keeperVerificationPane.setManaged(true);
                }
                case "souls" -> {
                    soulModerationPane.setVisible(true);
                    soulModerationPane.setManaged(true);
                }
                case "other" -> {
                    otherSectionPane.setVisible(true);
                    otherSectionPane.setManaged(true);
                }
            }
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), contentBody);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    @FXML
    private void refreshPendingSignups() {
        // Show and rotate loading icon
        refreshLoadingIcon.setVisible(true);
        refreshLoadingIcon.setManaged(true);
        refreshBtn.setDisable(true);
        
        // Continuous rotation animation
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), refreshLoadingIcon);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        // Load data in background
        new Thread(() -> {
            try {
                List<KeeperRepository.KeeperSignupRequest> requests = KeeperRepository.getPendingSignups();
                
                Platform.runLater(() -> {
                    // Stop rotation
                    rotate.stop();
                    refreshLoadingIcon.setVisible(false);
                    refreshLoadingIcon.setManaged(false);
                    refreshLoadingIcon.setRotate(0);
                    refreshBtn.setDisable(false);
                    
                    // Update UI
                    pendingSignupsContainer.getChildren().clear();
                    
                    if (requests.isEmpty()) {
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                    } else {
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        
                        for (KeeperRepository.KeeperSignupRequest request : requests) {
                            pendingSignupsContainer.getChildren().add(createSignupCard(request));
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load pending signups: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    rotate.stop();
                    refreshLoadingIcon.setVisible(false);
                    refreshLoadingIcon.setManaged(false);
                    refreshLoadingIcon.setRotate(0);
                    refreshBtn.setDisable(false);
                    showAlert("Error", "Failed to load pending signups: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void loadPendingSignups() {
        // Show loading state
        pendingSignupsContainer.getChildren().clear();
        
        // Load in background thread
        new Thread(() -> {
            try {
                List<KeeperRepository.KeeperSignupRequest> requests = KeeperRepository.getPendingSignups();
                
                Platform.runLater(() -> {
                    pendingSignupsContainer.getChildren().clear();
                    
                    if (requests.isEmpty()) {
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                    } else {
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        
                        for (KeeperRepository.KeeperSignupRequest request : requests) {
                            pendingSignupsContainer.getChildren().add(createSignupCard(request));
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load pending signups: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load pending signups: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private VBox createSignupCard(KeeperRepository.KeeperSignupRequest request) {
        VBox card = new VBox(15);
        card.getStyleClass().add("signup-card");
        card.setPadding(new Insets(20));
        
        // Header with Keeper ID
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label keeperIdIcon = new Label("👤");
        keeperIdIcon.setStyle("-fx-font-size: 24px;");
        
        Label keeperId = new Label(request.keeperId);
        keeperId.getStyleClass().add("card-keeper-id");
        keeperId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(keeperIdIcon, keeperId);
        
        // Email
        HBox emailBox = new HBox(8);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("card-label");
        emailLabel.setStyle("-fx-font-size: 12px;");
        
        Label emailValue = new Label(request.email);
        emailValue.getStyleClass().add("card-email");
        emailValue.setStyle("-fx-font-size: 14px;");
        
        emailBox.getChildren().addAll(emailLabel, emailValue);
        
        // Created date
        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label("Requested:");
        dateLabel.getStyleClass().add("card-label");
        dateLabel.setStyle("-fx-font-size: 12px;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dateValue = new Label(request.createdAt.format(formatter));
        dateValue.getStyleClass().add("card-date");
        dateValue.setStyle("-fx-font-size: 12px;");
        
        dateBox.getChildren().addAll(dateLabel, dateValue);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button approveBtn = new Button("✓ Approve");
        approveBtn.getStyleClass().add("approve-button");
        approveBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 120px; -fx-pref-height: 35px;");
        approveBtn.setOnAction(e -> handleApprove(request));
        
        Button rejectBtn = new Button("✗ Reject");
        rejectBtn.getStyleClass().add("reject-button");
        rejectBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 120px; -fx-pref-height: 35px;");
        rejectBtn.setOnAction(e -> handleReject(request));
        
        buttonBox.getChildren().addAll(rejectBtn, approveBtn);
        
        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);");
        
        card.getChildren().addAll(header, emailBox, dateBox, divider, buttonBox);
        
        return card;
    }
    
    private void handleApprove(KeeperRepository.KeeperSignupRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Keeper");
        confirm.setHeaderText("Approve " + request.keeperId + "?");
        confirm.setContentText("This will grant keeper access to " + request.keeperId + " and send them an approval email.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Disable button to prevent double-clicks
                new Thread(() -> {
                    try {
                        // Approve signup
                        KeeperRepository.approveSignup(request.keeperId, currentKeeperId);
                        
                        // Send approval email
                        EmailService.sendKeeperApprovalNotification(request.email, request.keeperId);
                        
                        Platform.runLater(() -> {
                            showAlert("Success", "Keeper " + request.keeperId + " has been approved!\n\nApproval email sent to " + request.email);
                            loadPendingSignups(); // Refresh list
                        });
                    } catch (Exception e) {
                        System.err.println("Failed to approve keeper: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to approve keeper: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }
    
    private void handleReject(KeeperRepository.KeeperSignupRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reject Keeper");
        confirm.setHeaderText("Reject " + request.keeperId + "?");
        confirm.setContentText("This will reject the keeper signup request. This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implement reject functionality
                showAlert("Info", "Reject functionality to be implemented");
            }
        });
    }
    
    @FXML
    private void loadSoulModeration() {
        // Show loading state
        soulsListContainer.getChildren().clear();
        soulsEmptyState.setVisible(false);
        soulsEmptyState.setManaged(false);
        
        // Disable refresh button while loading
        if (refreshSoulsBtn != null) {
            refreshSoulsBtn.setDisable(true);
        }
        
        // Load in background thread
        new Thread(() -> {
            try {
                List<KeeperRepository.SoulInfo> souls = KeeperRepository.getAllSouls();
                
                Platform.runLater(() -> {
                    soulsListContainer.getChildren().clear();
                    
                    if (souls.isEmpty()) {
                        soulsEmptyState.setVisible(true);
                        soulsEmptyState.setManaged(true);
                        activeSoulsCount.setText("0");
                        inactiveSoulsCount.setText("0");
                        totalSoulsCount.setText("0");
                    } else {
                        soulsEmptyState.setVisible(false);
                        soulsEmptyState.setManaged(false);
                        
                        // Count active and inactive souls
                        long activeCount = souls.stream().filter(s -> s.isActive).count();
                        long inactiveCount = souls.size() - activeCount;
                        
                        // Update stats
                        activeSoulsCount.setText(String.valueOf(activeCount));
                        inactiveSoulsCount.setText(String.valueOf(inactiveCount));
                        totalSoulsCount.setText(String.valueOf(souls.size()));
                        
                        // Create soul cards
                        for (KeeperRepository.SoulInfo soul : souls) {
                            soulsListContainer.getChildren().add(createSoulCard(soul));
                        }
                    }
                    
                    // Re-enable refresh button
                    if (refreshSoulsBtn != null) {
                        refreshSoulsBtn.setDisable(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load souls: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load souls: " + e.getMessage());
                    if (refreshSoulsBtn != null) {
                        refreshSoulsBtn.setDisable(false);
                    }
                });
            }
        }).start();
    }
    
    private HBox createSoulCard(KeeperRepository.SoulInfo soul) {
        HBox card = new HBox(20);
        card.getStyleClass().add("soul-card");
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Status indicator (circle)
        Circle statusCircle = new Circle(8);
        if (soul.isActive) {
            statusCircle.setStyle("-fx-fill: #7bed9f;"); // Green for active
        } else {
            statusCircle.setStyle("-fx-fill: #ff6b6b;"); // Red for inactive
        }
        
        // Soul info section
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        // Soul ID and Name
        Label nameLabel = new Label(soul.soulName != null ? soul.soulName : "Unknown");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.getStyleClass().add("soul-name-label");
        
        Label idLabel = new Label("ID: " + soul.soulId);
        idLabel.setStyle("-fx-font-size: 12px;");
        idLabel.getStyleClass().add("soul-id-label");
        
        infoBox.getChildren().addAll(nameLabel, idLabel);
        
        // Status section
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPrefWidth(150);
        
        Label statusLabel = new Label(soul.isActive ? "🟢 Active" : "🔴 Inactive");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusLabel.getStyleClass().add("soul-status-label");
        
        statusBox.getChildren().add(statusLabel);
        
        // Last activity section
        VBox activityBox = new VBox(5);
        activityBox.setAlignment(Pos.CENTER_RIGHT);
        activityBox.setPrefWidth(200);
        
        Label activityLabel = new Label("Last Activity");
        activityLabel.setStyle("-fx-font-size: 11px;");
        activityLabel.getStyleClass().add("soul-activity-label");
        
        String activityText;
        if (soul.lastActivity == null) {
            activityText = "Never logged in";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            activityText = soul.lastActivity.format(formatter);
        }
        
        Label activityValue = new Label(activityText);
        activityValue.setStyle("-fx-font-size: 13px;");
        activityValue.getStyleClass().add("soul-activity-value");
        
        activityBox.getChildren().addAll(activityLabel, activityValue);
        
        card.getChildren().addAll(statusCircle, infoBox, statusBox, activityBox);
        
        return card;
    }
    
    @FXML
    private void goToKeeperProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/keeper_profile.fxml"));
            Parent profileRoot = loader.load();
            
            // Pass keeper info to profile controller
            KeeperProfileController controller = loader.getController();
            controller.setKeeperInfo(currentKeeperId);
            
            root.getScene().setRoot(profileRoot);
        } catch (Exception e) {
            System.err.println("Failed to load keeper profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load profile page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be redirected to the login page.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/admin_login.fxml"));
                    Parent loginRoot = loader.load();
                    root.getScene().setRoot(loginRoot);
                } catch (Exception e) {
                    System.err.println("Failed to load login page: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void handleThemeToggle() {
        com.the_pathfinders.util.ThemeManager.toggleTheme();
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.95);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
        fadeIn.setFromValue(0.95);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> {
            com.the_pathfinders.util.ThemeManager.applyTheme(root);
            if (com.the_pathfinders.util.ThemeManager.isLightMode()) {
                themeToggleButton.setText("🌙");
            } else {
                themeToggleButton.setText("☀");
            }
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
