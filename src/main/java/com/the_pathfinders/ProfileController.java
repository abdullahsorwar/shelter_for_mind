package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import com.the_pathfinders.db.SoulInfoRepository;
import com.the_pathfinders.db.SoulInfoRepository.SoulInfo;
import com.the_pathfinders.util.JournalUtils;
import com.the_pathfinders.verification.VerificationManager;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class ProfileController {
    // Root + structural containers
    @FXML private AnchorPane root;
    @FXML private AnchorPane contentWrapper; // blurred only
    @FXML private VBox sidePanel;
    @FXML private Button menuBtn;
    @FXML private VBox menuItems;
    @FXML private Button basicInfoMenuBtn;
    @FXML private Button journalsMenuBtn;
    @FXML private AnchorPane mainArea;
    @FXML private HBox headerBox;
    @FXML private ImageView profileImage;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label starredBadge;
    @FXML private Button backBtn;
    @FXML private StackPane pagesContainer;

    // Pages
    @FXML private ScrollPane basicInfoScroll;
    @FXML private VBox basicInfoFields;
    @FXML private VBox journalsPage;
    @FXML private VBox journalsList;
    @FXML private VBox savedBlogsPage;
    @FXML private VBox savedBlogsList;
    @FXML private VBox savedJournalsPage;
    @FXML private VBox savedJournalsList;

    // Menu buttons
    @FXML private Button savedJournalsMenuBtn;
    @FXML private Button savedBlogsMenuBtn;

    // Edit/save controls
    @FXML private Button editInfoBtn;
    @FXML private Button saveInfoBtn;
    @FXML private Button verifyEmailBtn;

    // First-time overlay
    @FXML private StackPane firstTimeOverlay;
    @FXML private VBox overlayContentBox;
    @FXML private Button overlayYesBtn;
    @FXML private Button overlayNoBtn;
    @FXML private ImageView warningIcon;

    private String soulId;
    private final SoulInfoRepository soulInfoRepo = new SoulInfoRepository();
    private final JournalRepository journalRepo = new JournalRepository();
    private final SavedBlogsManager savedBlogsManager = new SavedBlogsManager();
    private final SavedJournalsManager savedJournalsManager = new SavedJournalsManager();
    private SoulInfo currentInfo;
    private final List<InfoRow> rows = new ArrayList<>();
    private boolean editMode = false;

    // Real-time update tracking
    private final Map<String, Label> timestampLabels = new HashMap<>();
    private final Map<String, Label> loveCountLabels = new HashMap<>();
    private final Map<String, Journal> journalDataMap = new HashMap<>();
    private Timeline timestampTimeline;
    private Timeline loveCountTimeline;
    
    // WebSocket for email verification
    private WebSocketClient verificationWebSocket;
    private Alert emailSentAlert; // Store reference to dismiss when verified

    private static class InfoRow {
        Label attrLabel;
        Label valueLabel;     // View mode
        TextField valueField; // Edit mode (for text fields)
        ComboBox<String> valueCombo; // Edit mode (for dropdowns)
        String key;
        boolean isComboBox;
    }

    public void setSoulId(String id) { 
        this.soulId = id == null ? "" : id; 
        System.out.println("ProfileController.setSoulId called with: " + this.soulId);
    }

    @FXML
    public void initialize() {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
            backBtn.getStyleClass().add("back-btn");
        }
        if (editInfoBtn != null) {
            editInfoBtn.setOnAction(e -> toggleEdit());
            editInfoBtn.getStyleClass().add("edit-btn");
        }
        if (saveInfoBtn != null) {
            saveInfoBtn.setOnAction(e -> saveInfo());
            saveInfoBtn.getStyleClass().add("save-btn");
            saveInfoBtn.setDisable(true);
            saveInfoBtn.setOpacity(0.4);
        }
        if (verifyEmailBtn != null) {
            verifyEmailBtn.setOnAction(e -> onVerifyEmail());
            verifyEmailBtn.getStyleClass().add("verify-btn");
            verifyEmailBtn.setVisible(false);
            verifyEmailBtn.setManaged(false);
        }
        if (overlayYesBtn != null) overlayYesBtn.setOnAction(e -> onFirstTimeYes());
        if (overlayNoBtn != null) overlayNoBtn.setOnAction(e -> onFirstTimeNo());
        if (menuBtn != null) {
            menuBtn.setOnAction(e -> toggleSidePanel());
            // Replace text with menu icon
            try {
                java.net.URL iconUrl = getClass().getResource("/assets/icons/menu.png");
                if (iconUrl != null) {
                    javafx.scene.image.Image menuImage = new javafx.scene.image.Image(iconUrl.toExternalForm());
                    javafx.scene.image.ImageView menuIcon = new javafx.scene.image.ImageView(menuImage);
                    menuIcon.setFitWidth(24);
                    menuIcon.setFitHeight(24);
                    menuIcon.setPreserveRatio(true);
                    menuBtn.setGraphic(menuIcon);
                    menuBtn.setText("");
                }
            } catch (Exception ex) {
                System.err.println("Could not load menu icon: " + ex.getMessage());
            }
        }
        if (basicInfoMenuBtn != null) basicInfoMenuBtn.setOnAction(e -> showBasicInfo());
        if (journalsMenuBtn != null) journalsMenuBtn.setOnAction(e -> showJournals());
        if (savedJournalsMenuBtn != null) savedJournalsMenuBtn.setOnAction(e -> showSavedJournals());
        if (savedBlogsMenuBtn != null) savedBlogsMenuBtn.setOnAction(e -> showSavedBlogs());

        // Keep mainArea left anchor in sync with sidePanel width (so it shrinks/expands)
        if (sidePanel != null && mainArea != null) {
            AnchorPane.setLeftAnchor(mainArea, sidePanel.getPrefWidth());
            sidePanel.widthProperty().addListener((obs, ov, nv) -> AnchorPane.setLeftAnchor(mainArea, nv.doubleValue()));
        }
    }

    @FXML
    public void onShown() {
        // Called after soulId is set externally
        System.out.println("ProfileController.onShown called with soulId: " + soulId);
        loadProfileImage();
        checkFirstTimeAndLoad();
        // Default page
        showBasicInfo();
    }

    private void loadProfileImage() {
        try {
            URL u = getClass().getResource("/com/the_pathfinders/" + soulId + ".jpg");
            if (u == null) u = getClass().getResource("/assets/icons/user.png");
            if (u != null) profileImage.setImage(new Image(u.toExternalForm(), 64, 64, true, true));
        } catch (Exception ignored) {}
    }

    private void checkFirstTimeAndLoad() {
        new Thread(() -> {
            try {
                boolean exists = soulInfoRepo.exists(soulId);
                if (!exists) {
                    Platform.runLater(() -> showFirstTimeOverlayAnimated());
                } else {
                    currentInfo = soulInfoRepo.getBySoulId(soulId);
                    Platform.runLater(this::renderInfoRows);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void showFirstTimeOverlayAnimated() {
        firstTimeOverlay.setVisible(true);
        firstTimeOverlay.setManaged(true);
        
        // Set the warning icon
        setWarningIcon();
        
        // Background blur only on content wrapper (keeps overlay sharp)
        javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(0);
        if (contentWrapper != null) contentWrapper.setEffect(blur);
        javafx.animation.Timeline blurTl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), new javafx.animation.KeyValue(blur.radiusProperty(), 0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(280), new javafx.animation.KeyValue(blur.radiusProperty(), 18))
        );
        blurTl.play();

        // Overlay fade + scale
        firstTimeOverlay.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(240), firstTimeOverlay);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();

        if (overlayContentBox != null) {
            // Constrain to a small centered rectangle
            overlayContentBox.setPrefWidth(520);
            overlayContentBox.setMaxWidth(540);
            overlayContentBox.setMinWidth(420);
            overlayContentBox.setPrefHeight(260);
            overlayContentBox.setMaxHeight(Region.USE_PREF_SIZE);
            overlayContentBox.setScaleX(0.96);
            overlayContentBox.setScaleY(0.96);
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(260), overlayContentBox);
            st.setFromX(0.96); st.setFromY(0.96); st.setToX(1); st.setToY(1);
            st.play();
        }
    }

    private void onFirstTimeYes() {
        // Fetch auth record for defaults and enable edit mode
        new Thread(() -> {
            try {
                var auth = soulInfoRepo.getAuthRecord(soulId);
                soulInfoRepo.insertBasic(soulId, auth == null ? null : auth.name(), auth == null ? null : auth.dob(), auth == null ? null : auth.mobile(), auth == null ? null : auth.countryCode());
                currentInfo = soulInfoRepo.getBySoulId(soulId);
                Platform.runLater(() -> {
                    hideFirstTimeOverlayAnimated();
                    renderInfoRows();
                    toggleEdit(); // Auto-enable edit mode
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void onFirstTimeNo() {
        new Thread(() -> {
            try {
                var auth = soulInfoRepo.getAuthRecord(soulId);
                soulInfoRepo.insertBasic(soulId, auth == null ? null : auth.name(), auth == null ? null : auth.dob(), auth == null ? null : auth.mobile(), auth == null ? null : auth.countryCode());
                currentInfo = soulInfoRepo.getBySoulId(soulId);
                Platform.runLater(() -> {
                    hideFirstTimeOverlayAnimated();
                    renderInfoRows();
                    // Don't enable edit mode
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void setWarningIcon() {
        try {
            if (warningIcon != null) {
                URL u = getClass().getResource("/assets/icons/exc.png");
                if (u != null) warningIcon.setImage(new Image(u.toExternalForm(), 48, 48, true, true));
            }
        } catch (Exception ignored) {}
    }

    private void hideFirstTimeOverlayAnimated() {
        // Reverse blur and fade out overlay
        javafx.scene.effect.Effect eff = contentWrapper == null ? null : contentWrapper.getEffect();
        if (eff instanceof javafx.scene.effect.GaussianBlur blur) {
            javafx.animation.Timeline blurTl = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), new javafx.animation.KeyValue(blur.radiusProperty(), blur.getRadius())),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(220), new javafx.animation.KeyValue(blur.radiusProperty(), 0))
            );
            blurTl.setOnFinished(e -> contentWrapper.setEffect(null));
            blurTl.play();
        }
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), firstTimeOverlay);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> { firstTimeOverlay.setVisible(false); firstTimeOverlay.setManaged(false); });
        ft.play();
    }

    private void renderInfoRows() {
        basicInfoFields.getChildren().clear();
        rows.clear();
        makeRow("Name", currentInfo == null ? "" : safe(currentInfo.name), "name", false);
        makeRow("Date of Birth", currentInfo == null || currentInfo.dob == null ? "" : currentInfo.dob.toString(), "dob", false);
        makeRow("Email", currentInfo == null ? "" : safe(currentInfo.email), "email", false);
        makeRow("Phone Number", currentInfo == null ? "" : safe(currentInfo.phone), "phone", false);
        makeRow("Home Address", currentInfo == null ? "" : safe(currentInfo.address), "address", false);
        makeRow("Country Code", currentInfo == null ? "" : safe(currentInfo.countryCode), "country_code", true);
        
        // Load and display Safety Plan
        loadAndDisplaySafetyPlan();
        
        // Show verify button if email exists, is valid, but NOT verified
        String email = currentInfo == null ? "" : safe(currentInfo.email);
        boolean emailVerified = currentInfo != null && currentInfo.emailVerified != null && currentInfo.emailVerified;
        
        if (email != null && !email.isEmpty() && isValidEmail(email) && !emailVerified) {
            updateVerifyButtonVisibility(email);
        }
    }

    private void makeRow(String attr, String value, String key, boolean isComboBox) {
        InfoRow r = new InfoRow();
        r.attrLabel = new Label(attr + ":");
        r.attrLabel.getStyleClass().add("info-attr");
        r.isComboBox = isComboBox;
        
        // View mode label - make it full width
        r.valueLabel = new Label(value);
        r.valueLabel.getStyleClass().add("info-value-label");
        r.valueLabel.setWrapText(true);
        r.valueLabel.setMaxWidth(Double.MAX_VALUE);
        r.valueLabel.setVisible(!editMode);
        r.valueLabel.setManaged(!editMode);
        
        if (isComboBox) {
            // Edit mode ComboBox for country code
            r.valueCombo = new ComboBox<>();
            r.valueCombo.getItems().setAll("+1 (USA)", "+91 (IND)", "+880 (BAN)");
            r.valueCombo.getStyleClass().add("combo-box");
            r.valueCombo.setMaxWidth(Double.MAX_VALUE);
            r.valueCombo.setVisible(editMode);
            r.valueCombo.setManaged(editMode);
            
            // Set initial value
            if (value != null && !value.isEmpty()) {
                r.valueCombo.setValue(value);
            } else {
                r.valueCombo.getSelectionModel().selectFirst();
            }
            
            VBox box = new VBox(6, r.attrLabel, r.valueLabel, r.valueCombo);
            box.getStyleClass().add("info-item");
            basicInfoFields.getChildren().add(box);
        } else {
            // Edit mode TextField for regular fields
            r.valueField = new TextField(value);
            r.valueField.getStyleClass().add("info-value-field");
            r.valueField.setVisible(editMode);
            r.valueField.setManaged(editMode);
            
            VBox box = new VBox(6, r.attrLabel, r.valueLabel, r.valueField);
            box.getStyleClass().add("info-item");
            basicInfoFields.getChildren().add(box);
        }
        
        r.key = key;
        rows.add(r);
    }

    private void toggleEdit() {
        boolean previousEditMode = editMode;
        editMode = !editMode;
        
        for (InfoRow r : rows) {
            // Toggle visibility between label and field/combo
            r.valueLabel.setVisible(!editMode);
            r.valueLabel.setManaged(!editMode);
            
            if (r.isComboBox && r.valueCombo != null) {
                r.valueCombo.setVisible(editMode);
                r.valueCombo.setManaged(editMode);
                
                // Sync combo with label value when entering edit mode
                if (editMode) {
                    String labelText = r.valueLabel.getText();
                    if (labelText != null && !labelText.isEmpty()) {
                        r.valueCombo.setValue(labelText);
                    }
                }
            } else if (r.valueField != null) {
                r.valueField.setVisible(editMode);
                r.valueField.setManaged(editMode);
                
                // When entering edit mode, sync field with saved database value (from label)
                // When exiting edit mode (cancel), revert field to saved value
                if (editMode) {
                    // Entering edit mode: load saved value from database (label)
                    r.valueField.setText(r.valueLabel.getText());
                } else if (previousEditMode) {
                    // Exiting edit mode (cancel): revert to saved value
                    r.valueField.setText(r.valueLabel.getText());
                }
            }
        }
        saveInfoBtn.setDisable(!editMode);
        saveInfoBtn.setOpacity(editMode ? 1.0 : 0.4);
        editInfoBtn.setText(editMode ? "Cancel" : "Edit");
        
        // Hide verify button when entering edit mode
        if (editMode && verifyEmailBtn != null) {
            verifyEmailBtn.setVisible(false);
            verifyEmailBtn.setManaged(false);
        }
    }

    private void saveInfo() {
        // Collect values
        String name = getValue("name");
        final LocalDate dob; LocalDate tmpDob = null; try { tmpDob = getValue("dob").isEmpty() ? null : LocalDate.parse(getValue("dob")); } catch (Exception ignored) {}
        dob = tmpDob;
        String email = getValue("email");
        String phone = getValue("phone");
        String address = getValue("address");
        String countryCode = getValue("country_code");

        // Validate all fields - if validation fails, stay in edit mode
        if (!validateAllFields(name, email, phone)) {
            return; // Validation failed, stay in edit mode, don't save anything
        }

        saveInfoBtn.setDisable(true);
        new Thread(() -> {
            try {
                soulInfoRepo.upsert(soulId, name, dob, email, phone, address, countryCode);
                // Reload current info to get updated emailVerified status
                currentInfo = soulInfoRepo.getBySoulId(soulId);
                
                Platform.runLater(() -> {
                    // Update view labels with new values
                    for (InfoRow r : rows) {
                        if (r.isComboBox && r.valueCombo != null) {
                            r.valueLabel.setText(r.valueCombo.getValue());
                        } else if (r.valueField != null) {
                            r.valueLabel.setText(r.valueField.getText());
                        }
                    }
                    
                    // Exit edit mode first
                    if (editMode) toggleEdit();
                    
                    // Update verify button visibility based on latest verification status
                    String savedEmail = currentInfo != null ? currentInfo.email : null;
                    if (savedEmail != null && !savedEmail.isEmpty() && isValidEmail(savedEmail)) {
                        boolean emailVerified = currentInfo.emailVerified != null && currentInfo.emailVerified;
                        System.out.println("After save - Email: " + savedEmail + ", Verified: " + emailVerified);
                        
                        // Show button if email is not verified
                        verifyEmailBtn.setVisible(!emailVerified);
                        verifyEmailBtn.setManaged(!emailVerified);
                    } else {
                        // Hide button if no valid email
                        verifyEmailBtn.setVisible(false);
                        verifyEmailBtn.setManaged(false);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to save info: " + ex.getMessage(), ButtonType.OK);
                    a.showAndWait();
                    saveInfoBtn.setDisable(false);
                });
            }
        }).start();
    }

    private String getValue(String key) {
        for (InfoRow r : rows) {
            if (r.key.equals(key)) {
                if (r.isComboBox && r.valueCombo != null) {
                    String val = r.valueCombo.getValue();
                    return val == null ? "" : val.trim();
                } else if (r.valueField != null) {
                    return r.valueField.getText().trim();
                }
            }
        }
        return "";
    }

    private void loadJournals() {
        if (journalsList == null) return;
        
        // Stop existing timelines
        stopRealTimeUpdates();
        
        // Clear previous data
        journalsList.getChildren().clear();
        timestampLabels.clear();
        loveCountLabels.clear();
        journalDataMap.clear();
        
        new Thread(() -> {
            try {
                List<com.the_pathfinders.Journal> userJournals = journalRepo.getJournalsBySoulId(soulId);
                Platform.runLater(() -> {
                    for (var j : userJournals) {
                        journalsList.getChildren().add(createJournalBox(j));
                    }
                    
                    // Start real-time updates
                    startRealTimeUpdates();
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    
    
    private void startRealTimeUpdates() {
        // Timestamp updates every 1 second
        timestampTimeline = JournalUtils.createTimestampUpdateTimeline(timestampLabels, journalDataMap);
        timestampTimeline.play();
        
        // Love count updates every 2 seconds
        loveCountTimeline = JournalUtils.createLoveCountUpdateTimeline(loveCountLabels, journalDataMap);
        loveCountTimeline.play();
    }
    
    private void stopRealTimeUpdates() {
        if (timestampTimeline != null) {
            timestampTimeline.stop();
            timestampTimeline = null;
        }
        if (loveCountTimeline != null) {
            loveCountTimeline.stop();
            loveCountTimeline = null;
        }
    }

    private void loadSavedBlogs() {
        if (savedBlogsList == null) return;
        
        savedBlogsList.getChildren().clear();
        
        new Thread(() -> {
            try {
                // Load all available blogs (predefined list from BlogController)
                java.util.List<Blog> allBlogs = new java.util.ArrayList<>();
                java.util.List<String> categories = java.util.Arrays.asList(
                    "Depression", "Anxiety Disorders", "Bipolar Disorder", "Schizophrenia",
                    "Obsessive-Compulsive Disorder", "Post-Traumatic Stress Disorder",
                    "Eating Disorder", "Generalized Anxiety Disorder", "Neurodevelopmental Disorder",
                    "ADHD", "Panic Disorder", "Social Anxiety Disorder"
                );
                
                int i = 1;
                for (String cat : categories) {
                    String shortIntro = "A short introduction to " + cat + ".";
                    Blog blog = new Blog("b" + i, cat + " — Understanding", shortIntro, cat);
                    String full = com.the_pathfinders.BlogContentLoader.loadContentForCategory(cat, "A comprehensive guide to understanding " + cat + ".");
                    blog.setFullDescription(full);
                    allBlogs.add(blog);
                    i++;
                }
                
                // Get saved blog IDs for this user
                java.util.Set<String> savedIds = savedBlogsManager.loadSavedBlogIds(soulId);
                
                Platform.runLater(() -> {
                    for (Blog blog : allBlogs) {
                        if (savedIds.contains(blog.getId())) {
                            savedBlogsList.getChildren().add(createSavedBlogBox(blog));
                        }
                    }
                    
                    // If no saved blogs, show a message
                    if (savedBlogsList.getChildren().isEmpty()) {
                        Label noSavedLabel = new Label("No saved blogs yet. Start saving blogs from the Blog section!");
                        noSavedLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                        savedBlogsList.getChildren().add(noSavedLabel);
                    }
                });
            } catch (Exception ex) { 
                ex.printStackTrace(); 
            }
        }).start();
    }

    private void loadSavedJournals() {
        if (savedJournalsList == null) return;
        
        savedJournalsList.getChildren().clear();
        
        new Thread(() -> {
            try {
                // Load all public journals from database
                com.the_pathfinders.db.JournalRepository journalRepo = new com.the_pathfinders.db.JournalRepository();
                java.util.List<Journal> allJournals = journalRepo.getAllPublicJournals();
                
                // Get saved journal IDs for this user
                java.util.Set<String> savedIds = savedJournalsManager.loadSavedJournalIds(soulId);
                
                Platform.runLater(() -> {
                    for (Journal journal : allJournals) {
                        if (savedIds.contains(String.valueOf(journal.getId()))) {
                            savedJournalsList.getChildren().add(createSavedJournalBox(journal));
                        }
                    }
                    
                    // If no saved journals, show a message
                    if (savedJournalsList.getChildren().isEmpty()) {
                        Label noSavedLabel = new Label("No saved journals yet. Start saving journals from the Public Journals section!");
                        noSavedLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                        savedJournalsList.getChildren().add(noSavedLabel);
                    }
                });
            } catch (Exception ex) { 
                ex.printStackTrace(); 
            }
        }).start();
    }

    private VBox createSavedBlogBox(Blog blog) {
        // Outer box with spacing
        VBox outerBox = new VBox();
        outerBox.getStyleClass().add("saved-blog-outer-box");
        outerBox.setPadding(new javafx.geometry.Insets(12));
        outerBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-spacing: 8;");

        // Header with title + category + expand/collapse button
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 8; -fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label title = new Label(blog.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #264653;");

        Label category = new Label(blog.getCategory());
        category.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Expandable content area (initially hidden)
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new javafx.geometry.Insets(12));
        contentBox.setStyle("-fx-background-color: #ffffff;");
        contentBox.setVisible(false);
        contentBox.setManaged(false);

        // Full blog content
        Label content = new Label(blog.getFullDescription() != null ? blog.getFullDescription() : blog.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-line-spacing: 2px;");

        // Remove button
        Button removeBtn = new Button("✕ Remove");
        removeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px; -fx-background-color: #ffcccc; -fx-text-fill: #c0392b; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            savedBlogsManager.removeSavedBlog(soulId, blog.getId());
            savedBlogsList.getChildren().remove(outerBox);
            
            // Show message if no saved blogs left
            if (savedBlogsList.getChildren().isEmpty()) {
                Label noSavedLabel = new Label("No saved blogs yet. Start saving blogs from the Blog section!");
                noSavedLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                savedBlogsList.getChildren().add(noSavedLabel);
            }
        });

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getChildren().add(removeBtn);

        contentBox.getChildren().addAll(content, actions);

        // Expand/collapse button
        Button expandBtn = new Button("▼");
        expandBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4px 8px; -fx-background-color: transparent; -fx-cursor: hand;");

        expandBtn.setOnAction(e -> {
            if (contentBox.isVisible()) {
                contentBox.setVisible(false);
                contentBox.setManaged(false);
                expandBtn.setText("▶");
            } else {
                contentBox.setVisible(true);
                contentBox.setManaged(true);
                expandBtn.setText("▼");
            }
        });

        header.getChildren().addAll(expandBtn, title, category, spacer);

        outerBox.getChildren().addAll(header, contentBox);

        return outerBox;
    }

    private VBox createSavedJournalBox(Journal journal) {
        // Outer box with spacing
        VBox outerBox = new VBox();
        outerBox.getStyleClass().add("saved-journal-outer-box");
        outerBox.setPadding(new javafx.geometry.Insets(12));
        outerBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-spacing: 8;");

        // Header with author + date + expand/collapse button
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 8; -fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label author = new Label("@" + journal.getSoulId());
        author.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #264653;");

        Label date = new Label(journal.getEntryDate() != null ? journal.getEntryDate().toString() : "");
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Expandable content area (initially hidden)
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new javafx.geometry.Insets(12));
        contentBox.setStyle("-fx-background-color: #ffffff;");
        contentBox.setVisible(false);
        contentBox.setManaged(false);

        // Full journal content
        Label content = new Label(journal.getText());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-line-spacing: 2px;");

        // Remove button
        Button removeBtn = new Button("✕ Remove");
        removeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 12px; -fx-background-color: #ffcccc; -fx-text-fill: #c0392b; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            savedJournalsManager.removeSavedJournal(soulId, String.valueOf(journal.getId()));
            savedJournalsList.getChildren().remove(outerBox);
            
            // Show message if no saved journals left
            if (savedJournalsList.getChildren().isEmpty()) {
                Label noSavedLabel = new Label("No saved journals yet. Start saving journals from the Public Journals section!");
                noSavedLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                savedJournalsList.getChildren().add(noSavedLabel);
            }
        });

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getChildren().add(removeBtn);

        contentBox.getChildren().addAll(content, actions);

        // Expand/collapse button
        Button expandBtn = new Button("▼");
        expandBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4px 8px; -fx-background-color: transparent; -fx-cursor: hand;");

        expandBtn.setOnAction(e -> {
            if (contentBox.isVisible()) {
                contentBox.setVisible(false);
                contentBox.setManaged(false);
                expandBtn.setText("▶");
            } else {
                contentBox.setVisible(true);
                contentBox.setManaged(true);
                expandBtn.setText("▼");
            }
        });

        header.getChildren().addAll(expandBtn, author, date, spacer);

        outerBox.getChildren().addAll(header, contentBox);

        return outerBox;
    }

    private VBox createJournalBox(com.the_pathfinders.Journal journal) {
        // Outer box (gray background)
        VBox outerBox = new VBox();
        outerBox.getStyleClass().add("journal-outer-box");
        outerBox.setPadding(new javafx.geometry.Insets(8));

        // Inner box (glassy white)
        VBox innerBox = new VBox(8);
        innerBox.getStyleClass().add("journal-inner-box");
        innerBox.setPadding(new javafx.geometry.Insets(12));

        // Header: user icon + username + timestamp
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView userIcon = new ImageView();
        try {
            URL iconUrl = getClass().getResource("/com/the_pathfinders/" + journal.getSoulId() + ".jpg");
            if (iconUrl == null) iconUrl = getClass().getResource("/assets/icons/user.png");
            if (iconUrl != null) userIcon.setImage(new Image(iconUrl.toExternalForm(), 40, 40, true, true));
        } catch (Exception ignored) {}
        userIcon.setFitWidth(40);
        userIcon.setFitHeight(40);
        userIcon.setPreserveRatio(true);

        VBox userInfo = new VBox(2);
        Label username = new Label(journal.getSoulId());
        username.getStyleClass().add("journal-username");
        Label timestamp = new Label(JournalUtils.getRelativeTime(journal.getCreatedAt()));
        timestamp.getStyleClass().add("journal-time");
        userInfo.getChildren().addAll(username, timestamp);

        header.getChildren().addAll(userIcon, userInfo);

        // Separator
        Separator sep = new Separator();
        sep.getStyleClass().add("journal-separator");

        // Journal text with applied font
        Label textLabel = new Label(journal.getText());
        textLabel.setWrapText(true);
        textLabel.getStyleClass().add("journal-text");
        
        // Apply saved font family and size (matching PublicJournalsController)
        String fontFamily = journal.getFontFamily() != null ? journal.getFontFamily() : "System";
        Integer fontSize = journal.getFontSize() != null ? journal.getFontSize() : 14;
        textLabel.setStyle(String.format("-fx-font-family: '%s'; -fx-font-size: %dpx;", fontFamily, fontSize));

        // Love section (button with heart icon + count)
        HBox loveBox = new HBox(8);
        loveBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Create heart icons
        ImageView heartOutline = JournalUtils.createHeartIcon("/assets/icons/heart_outline.png", 24);
        ImageView heartFilled = JournalUtils.createHeartIcon("/assets/icons/heart_filled.png", 24);
        
        // Love button
        Button loveBtn = new Button();
        loveBtn.getStyleClass().add("love-button");
        loveBtn.setBackground(javafx.scene.layout.Background.EMPTY);
        loveBtn.setBorder(javafx.scene.layout.Border.EMPTY);
        loveBtn.setPadding(javafx.geometry.Insets.EMPTY);
        loveBtn.setFocusTraversable(false); // Prevent scroll jumping
        loveBtn.setGraphic(heartOutline);

        int loveCount = journal.getLoveCount() != null ? journal.getLoveCount() : 
                        (journal.getLovedBy() == null ? 0 : journal.getLovedBy().length);
        journal.setLoveCount(loveCount);
        Label countLabel = new Label(String.valueOf(loveCount));
        countLabel.getStyleClass().add("love-count");

        // Set initial love state
        JournalUtils.setInitialLoveState(journal.getId(), soulId, loveBtn, heartOutline, heartFilled);

        // Love button click handler (aligned with public journal logic)
        loveBtn.setOnAction(e -> JournalUtils.toggleLove(journal.getId(), soulId, loveBtn, countLabel, journal, heartOutline, heartFilled));

        loveBox.getChildren().addAll(loveBtn, countLabel);

        // Track for real-time updates
        timestampLabels.put(journal.getId(), timestamp);
        loveCountLabels.put(journal.getId(), countLabel);
        journalDataMap.put(journal.getId(), journal);

        innerBox.getChildren().addAll(header, sep, textLabel, loveBox);
        outerBox.getChildren().add(innerBox);

        return outerBox;
    }

    /* Side panel logic */
    private boolean panelExpanded = false;
    private final double COLLAPSED_WIDTH = 60;
    private final double EXPANDED_WIDTH = 220;

    private void toggleSidePanel() {
        if (sidePanel == null) return;
        panelExpanded = !panelExpanded;
        double start = sidePanel.getWidth() <= 0 ? sidePanel.getPrefWidth() : sidePanel.getWidth();
        double end = panelExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        // Animate panel width
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(sidePanel.prefWidthProperty(), start)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), new javafx.animation.KeyValue(sidePanel.prefWidthProperty(), end))
        );
        // Animate mainArea left anchor in sync for smooth shrink/expand
        if (mainArea != null) {
            DoubleProperty left = new SimpleDoubleProperty(start);
            left.addListener((o, ov, nv) -> AnchorPane.setLeftAnchor(mainArea, nv.doubleValue()));
            javafx.animation.Timeline tl2 = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(left, start)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), new javafx.animation.KeyValue(left, end))
            );
            tl2.play();
        }
        tl.setOnFinished(e -> {
            if (menuItems != null) {
                if (panelExpanded) {
                    menuItems.setVisible(true); menuItems.setManaged(true);
                    menuItems.setOpacity(0);
                    javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), menuItems);
                    ft.setToValue(1); ft.play();
                } else {
                    menuItems.setVisible(false); menuItems.setManaged(false);
                }
            }
        });
        tl.play();
    }

    private void showBasicInfo() {
        if (subtitleLabel != null) subtitleLabel.setText("Basic Information");
        setPageVisibility(true, false, false, false);
        if (editInfoBtn != null) { editInfoBtn.setVisible(true); editInfoBtn.setManaged(true); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(true); saveInfoBtn.setManaged(true); }
    }
    private void showJournals() {
        if (subtitleLabel != null) subtitleLabel.setText("Journal");
        setPageVisibility(false, true, false, false);
        loadJournals();
        if (editInfoBtn != null) { editInfoBtn.setVisible(false); editInfoBtn.setManaged(false); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(false); saveInfoBtn.setManaged(false); }
    }
    private void showSavedBlogs() {
        if (subtitleLabel != null) subtitleLabel.setText("Starred Blogs");
        setPageVisibility(false, false, true, false);
        loadSavedBlogs();
        if (editInfoBtn != null) { editInfoBtn.setVisible(false); editInfoBtn.setManaged(false); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(false); saveInfoBtn.setManaged(false); }
    }
    private void showSavedJournals() {
        if (subtitleLabel != null) subtitleLabel.setText("Starred Journals");
        setPageVisibility(false, false, false, true);
        loadSavedJournals();
        if (editInfoBtn != null) { editInfoBtn.setVisible(false); editInfoBtn.setManaged(false); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(false); saveInfoBtn.setManaged(false); }
    }
    public void showStarredJournalsSection() {
        showSavedJournals();
    }

    private void setPageVisibility(boolean basic, boolean journal, boolean savedBlogs, boolean savedJournals) {
        if (basicInfoScroll != null) { basicInfoScroll.setVisible(basic); basicInfoScroll.setManaged(basic); }
        if (journalsPage != null) { journalsPage.setVisible(journal); journalsPage.setManaged(journal); }
        if (savedBlogsPage != null) { savedBlogsPage.setVisible(savedBlogs); savedBlogsPage.setManaged(savedBlogs); }
        if (savedJournalsPage != null) { savedJournalsPage.setVisible(savedJournals); savedJournalsPage.setManaged(savedJournals); }
    }

    private void loadAndDisplaySafetyPlan() {
        if (soulId == null || soulId.isBlank()) return;
        
        try {
            com.the_pathfinders.db.SoulRepository soulRepo = new com.the_pathfinders.db.SoulRepository();
            java.util.Map<String, String> safetyPlan = soulRepo.getSafetyPlan(soulId);
            
            if (safetyPlan != null && (safetyPlan.get("contact") != null || safetyPlan.get("calm") != null || safetyPlan.get("place") != null)) {
                // Add separator
                javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
                separator.getStyleClass().add("safety-plan-separator");
                separator.setStyle("-fx-padding: 10 0 10 0;");
                basicInfoFields.getChildren().add(separator);
                
                // Add Safety Plan header
                Label safetyPlanHeader = new Label("🛡️ Safety Plan");
                safetyPlanHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-padding: 10 0 5 0;");
                basicInfoFields.getChildren().add(safetyPlanHeader);
                
                // Add note
                Label noteLabel = new Label("Personal crisis support plan for immediate help");
                noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic; -fx-padding: 0 0 10 0;");
                noteLabel.setWrapText(true);
                basicInfoFields.getChildren().add(noteLabel);
                
                // Display each safety plan field as read-only
                if (safetyPlan.get("contact") != null && !safetyPlan.get("contact").isBlank()) {
                    VBox contactBox = createSafetyPlanField("Who can you contact?", safetyPlan.get("contact"));
                    basicInfoFields.getChildren().add(contactBox);
                }
                
                if (safetyPlan.get("calm") != null && !safetyPlan.get("calm").isBlank()) {
                    VBox calmBox = createSafetyPlanField("What helps you calm down?", safetyPlan.get("calm"));
                    basicInfoFields.getChildren().add(calmBox);
                }
                
                if (safetyPlan.get("place") != null && !safetyPlan.get("place").isBlank()) {
                    VBox placeBox = createSafetyPlanField("Where is your safe place?", safetyPlan.get("place"));
                    basicInfoFields.getChildren().add(placeBox);
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to load safety plan: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private VBox createSafetyPlanField(String question, String answer) {
        VBox box = new VBox(8);
        box.getStyleClass().add("safety-plan-item");
        box.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #c5e1a5; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label questionLabel = new Label(question);
        questionLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        Label answerLabel = new Label(answer);
        answerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #424242;");
        answerLabel.setWrapText(true);
        answerLabel.setMaxWidth(Double.MAX_VALUE);
        
        box.getChildren().addAll(questionLabel, answerLabel);
        return box;
    }

    private String safe(String s) { return s == null ? "" : s; }

    /* ========== Validation Methods ========== */
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Optional field (empty is valid)
        
        // Comprehensive email validation rules
        
        // 1. Basic format check: local@domain.extension
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return false;
        }
        
        // 2. Check length limits (total max 254 chars, local part max 64 chars)
        if (email.length() > 254) return false;
        String[] parts = email.split("@");
        if (parts.length != 2) return false;
        if (parts[0].length() > 64) return false;
        
        // 3. Local part (before @) validations
        String localPart = parts[0];
        // Cannot start or end with a dot
        if (localPart.startsWith(".") || localPart.endsWith(".")) return false;
        // Cannot have consecutive dots
        if (localPart.contains("..")) return false;
        
        // 4. Domain part (after @) validations
        String domainPart = parts[1];
        // Domain cannot start or end with a hyphen or dot
        if (domainPart.startsWith("-") || domainPart.endsWith("-")) return false;
        if (domainPart.startsWith(".") || domainPart.endsWith(".")) return false;
        // Domain must have at least one dot
        if (!domainPart.contains(".")) return false;
        // Domain cannot have consecutive dots
        if (domainPart.contains("..")) return false;
        
        // 5. TLD (top-level domain) validations
        String[] domainParts = domainPart.split("\\.");
        String tld = domainParts[domainParts.length - 1];
        // TLD must be 2-24 characters and only letters (real-world TLD limits)
        if (tld.length() < 2 || tld.length() > 24 || !tld.matches("^[a-zA-Z]+$")) return false;
        // TLD maximum 24 characters (practical limit for real-world TLDs)
        if (tld.length() > 24) return false;
        
        // 6. Each domain label validation
        for (String label : domainParts) {
            if (label.isEmpty()) return false;
            if (label.length() > 63) return false; // Max 63 chars per label
        }
        
        return true;
    }
    
    private boolean isValidName(String name) {
        if (name == null || name.isEmpty()) return true; // Optional field
        // Name must contain only letters, spaces, dots, and hyphens
        return name.matches("^[a-zA-Z][a-zA-Z .\\-]*[a-zA-Z]$");
    }
    
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return true; // Optional field
        // Phone must be between 8 and 12 digits
        return phone.matches("^[0-9]{8,12}$");
    }
    
    private boolean validateAllFields(String name, String email, String phone) {
        // Validate name
        if (!isValidName(name)) {
            showValidationError("Invalid Name", "Name must contain only letters, spaces, dots, and hyphens");
            return false;
        }
        
        // Validate email format with detailed error message
        // If email has content but is invalid, prevent saving
        if (!isValidEmail(email)) {
            String emailError = getEmailValidationError(email);
            showValidationError("Invalid Email", emailError + "\n\nPlease fix or clear the email field to continue.");
            return false;
        }
        
        // Validate phone number
        if (!isValidPhone(phone)) {
            showValidationError("Invalid Phone Number", "Phone number must be between 8 and 12 digits");
            return false;
        }
        
        // Note: verify button will be shown after successful save
        
        return true;
    }
    
    private String getEmailValidationError(String email) {
        if (email == null || email.isEmpty()) return "Email is required";
        
        if (email.length() > 254) {
            return "Email is too long (maximum 254 characters)";
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "Email must contain exactly one @ symbol";
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() > 64) {
            return "Email local part is too long (maximum 64 characters before @)";
        }
        
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return "Email cannot start or end with a dot (.)";
        }
        
        if (localPart.contains("..")) {
            return "Email cannot contain consecutive dots (..)";
        }
        
        if (!domainPart.contains(".")) {
            return "Email domain must include a dot (e.g., domain.com)";
        }
        
        if (domainPart.startsWith(".") || domainPart.endsWith(".") || 
            domainPart.startsWith("-") || domainPart.endsWith("-")) {
            return "Email domain cannot start or end with dot (.) or hyphen (-)";
        }
        
        if (domainPart.contains("..")) {
            return "Email domain cannot contain consecutive dots (..)";
        }
        
        String[] domainParts = domainPart.split("\\.");
        String tld = domainParts[domainParts.length - 1];
        
        if (tld.length() < 2) {
            return "Email domain extension must be at least 2 characters (e.g., .com, .org)";
        }
        
        if (tld.length() > 24) {
            return "Email domain extension is too long (maximum 24 characters)";
        }
        
        if (!tld.matches("^[a-zA-Z]+$")) {
            return "Email domain extension can only contain letters";
        }
        
        if (!localPart.matches("^[a-zA-Z0-9._%+-]+$")) {
            return "Email contains invalid characters (allowed: letters, numbers, . _ % + -)";
        }
        
        if (!domainPart.matches("^[a-zA-Z0-9.-]+$")) {
            return "Email domain contains invalid characters";
        }
        
        return "Email format is invalid. Use: email@domain.com";
    }
    
    private void updateVerifyButtonVisibility(String email) {
        if (verifyEmailBtn == null) return;
        
        // Show verify button only if:
        // 1. Email is valid and not empty
        // 2. Email is NOT already verified
        // 3. Not in edit mode
        boolean emailValid = email != null && !email.isEmpty() && isValidEmail(email);
        boolean emailNotVerified = currentInfo == null || currentInfo.emailVerified == null || !currentInfo.emailVerified;
        boolean shouldShow = emailValid && emailNotVerified && !editMode;
        
        verifyEmailBtn.setVisible(shouldShow);
        verifyEmailBtn.setManaged(shouldShow);
    }
    
    private void showValidationError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void onVerifyEmail() {
        String email = getValue("email");
        
        if (email == null || email.isEmpty() || !isValidEmail(email)) {
            showValidationError("Invalid Email", "Please enter a valid email address first.");
            return;
        }
        
        // Disable button while processing
        verifyEmailBtn.setDisable(true);
        verifyEmailBtn.setText("Sending...");
        
        // Send verification email in background thread
        new Thread(() -> {
            try {
                // Start servers and send email
                VerificationManager.getInstance().sendVerificationEmail(soulId, email);
                
                // Wait a bit for servers to fully start before connecting WebSocket
                Thread.sleep(500);
                
                // Now connect to WebSocket for real-time updates
                Platform.runLater(() -> connectVerificationWebSocket());
                
                Platform.runLater(() -> {
                    verifyEmailBtn.setText("Email Sent!");
                    
                    // Create and store alert reference
                    emailSentAlert = new Alert(Alert.AlertType.INFORMATION);
                    emailSentAlert.setTitle("Email Verification");
                    emailSentAlert.setHeaderText("Verification Email Sent");
                    emailSentAlert.setContentText("A verification email has been sent to: " + email + 
                        "\n\nPlease check your inbox and click the verification link." +
                        "\n\nThe verify button will disappear automatically once you verify.");
                    emailSentAlert.show(); // Use show() instead of showAndWait() so it's non-blocking
                    
                    // Re-enable button in case user needs to resend
                    verifyEmailBtn.setDisable(false);
                    verifyEmailBtn.setText("Verify Email");
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    verifyEmailBtn.setDisable(false);
                    verifyEmailBtn.setText("Verify Email");
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Verification Error");
                    alert.setHeaderText("Failed to Send Email");
                    alert.setContentText("Could not send verification email: " + e.getMessage() +
                        "\n\nPlease check your internet connection and try again.");
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void connectVerificationWebSocket() {
        try {
            if (verificationWebSocket != null && verificationWebSocket.isOpen()) {
                return; // Already connected
            }
            
            URI serverUri = new URI("ws://localhost:8081/" + soulId);
            
            System.out.println("Attempting to connect WebSocket to: " + serverUri);
            
            verificationWebSocket = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket connected for verification");
                }
                
                @Override
                public void onMessage(String message) {
                    if ("VERIFIED".equals(message)) {
                        System.out.println("Email verified via WebSocket!");
                        
                        // Reload data from database in background
                        new Thread(() -> {
                            try {
                                currentInfo = soulInfoRepo.getBySoulId(soulId);
                                System.out.println("Reloaded info - Email verified: " + 
                                    (currentInfo != null && currentInfo.emailVerified != null && currentInfo.emailVerified));
                                
                                Platform.runLater(() -> {
                                    // Close the "Email Sent" alert if it's still open
                                    if (emailSentAlert != null) {
                                        emailSentAlert.close();
                                        emailSentAlert = null;
                                    }
                                    
                                    // Hide verify button immediately
                                    verifyEmailBtn.setVisible(false);
                                    verifyEmailBtn.setManaged(false);
                                    
                                    // Show success message
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Email Verified");
                                    alert.setHeaderText("Success!");
                                    alert.setContentText("Your email has been verified successfully!");
                                    alert.showAndWait();
                                    
                                    // Close WebSocket
                                    close();
                                });
                            } catch (Exception e) {
                                System.err.println("Failed to reload info: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }
                
                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    
                    // Retry connection after 1 second if connection refused (server not ready)
                    if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
                        System.out.println("WebSocket server not ready, retrying in 1 second...");
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    verificationWebSocket = null;
                                    connectVerificationWebSocket();
                                });
                            } catch (InterruptedException ie) {
                                ie.printStackTrace();
                            }
                        }).start();
                    }
                }
            };
            
            verificationWebSocket.connect();
            
        } catch (Exception e) {
            System.err.println("Failed to connect WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void closeVerificationWebSocket() {
        if (verificationWebSocket != null && verificationWebSocket.isOpen()) {
            verificationWebSocket.close();
            verificationWebSocket = null;
        }
    }

    private void goBack() {
        // Stop real-time updates before leaving
        stopRealTimeUpdates();
        
        // Close verification WebSocket
        closeVerificationWebSocket();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController dc = loader.getController();
            dc.setUser(soulId, soulId);
            if (root.getScene() != null) root.getScene().setRoot(dash);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
