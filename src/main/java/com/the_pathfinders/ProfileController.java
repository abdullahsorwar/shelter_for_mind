package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import com.the_pathfinders.db.SoulInfoRepository;
import com.the_pathfinders.db.SoulInfoRepository.SoulInfo;
import com.the_pathfinders.util.JournalUtils;
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

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProfileController {
    // Root + structural containers
    @FXML private AnchorPane root;
    @FXML private AnchorPane contentWrapper; // blurred only
    @FXML private VBox sidePanel;
    @FXML private Button menuBtn;
    @FXML private VBox menuItems;
    @FXML private Button basicInfoMenuBtn;
    @FXML private Button journalsMenuBtn;
    @FXML private Button achievementsMenuBtn;
    @FXML private AnchorPane mainArea;
    @FXML private HBox headerBox;
    @FXML private ImageView profileImage;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button backBtn;
    @FXML private StackPane pagesContainer;

    // Pages
    @FXML private ScrollPane basicInfoScroll;
    @FXML private VBox basicInfoFields;
    @FXML private VBox journalsPage;
    @FXML private VBox journalsList;
    @FXML private VBox achievementsPage;

    // Edit/save controls
    @FXML private Button editInfoBtn;
    @FXML private Button saveInfoBtn;

    // First-time overlay
    @FXML private StackPane firstTimeOverlay;
    @FXML private VBox overlayContentBox;
    @FXML private Button overlayYesBtn;
    @FXML private Button overlayNoBtn;
    @FXML private ImageView warningIcon;

    private String soulId;
    private final SoulInfoRepository soulInfoRepo = new SoulInfoRepository();
    private final JournalRepository journalRepo = new JournalRepository();
    private SoulInfo currentInfo;
    private final List<InfoRow> rows = new ArrayList<>();
    private boolean editMode = false;

    // Real-time update tracking
    private final Map<String, Label> timestampLabels = new HashMap<>();
    private final Map<String, Label> loveCountLabels = new HashMap<>();
    private final Map<String, Journal> journalDataMap = new HashMap<>();
    private Timeline timestampTimeline;
    private Timeline loveCountTimeline;

    private static class InfoRow {
        Label attrLabel;
        Label valueLabel;     // View mode
        TextField valueField; // Edit mode (for text fields)
        ComboBox<String> valueCombo; // Edit mode (for dropdowns)
        String key;
        boolean isComboBox;
    }

    public void setSoulId(String id) { this.soulId = id == null ? "" : id; }

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
        if (overlayYesBtn != null) overlayYesBtn.setOnAction(e -> onFirstTimeYes());
        if (overlayNoBtn != null) overlayNoBtn.setOnAction(e -> onFirstTimeNo());
        if (menuBtn != null) menuBtn.setOnAction(e -> toggleSidePanel());
        if (basicInfoMenuBtn != null) basicInfoMenuBtn.setOnAction(e -> showBasicInfo());
        if (journalsMenuBtn != null) journalsMenuBtn.setOnAction(e -> showJournals());
        if (achievementsMenuBtn != null) achievementsMenuBtn.setOnAction(e -> showAchievements());

        // Keep mainArea left anchor in sync with sidePanel width (so it shrinks/expands)
        if (sidePanel != null && mainArea != null) {
            AnchorPane.setLeftAnchor(mainArea, sidePanel.getPrefWidth());
            sidePanel.widthProperty().addListener((obs, ov, nv) -> AnchorPane.setLeftAnchor(mainArea, nv.doubleValue()));
        }
    }

    @FXML
    public void onShown() {
        // Called after soulId is set externally
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
                
                // Sync field with label value when entering edit mode
                if (editMode) {
                    r.valueField.setText(r.valueLabel.getText());
                }
            }
        }
        saveInfoBtn.setDisable(!editMode);
        saveInfoBtn.setOpacity(editMode ? 1.0 : 0.4);
        editInfoBtn.setText(editMode ? "Cancel" : "Edit");
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

        saveInfoBtn.setDisable(true);
        new Thread(() -> {
            try {
                soulInfoRepo.upsert(soulId, name, dob, email, phone, address, countryCode);
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
                    // Exit edit mode
                    if (editMode) toggleEdit();
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
        setPageVisibility(true, false, false);
        if (editInfoBtn != null) { editInfoBtn.setVisible(true); editInfoBtn.setManaged(true); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(true); saveInfoBtn.setManaged(true); }
    }
    private void showJournals() {
        if (subtitleLabel != null) subtitleLabel.setText("Journal");
        setPageVisibility(false, true, false);
        loadJournals();
        if (editInfoBtn != null) { editInfoBtn.setVisible(false); editInfoBtn.setManaged(false); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(false); saveInfoBtn.setManaged(false); }
    }
    private void showAchievements() {
        if (subtitleLabel != null) subtitleLabel.setText("Achievements");
        setPageVisibility(false, false, true);
        if (editInfoBtn != null) { editInfoBtn.setVisible(false); editInfoBtn.setManaged(false); }
        if (saveInfoBtn != null) { saveInfoBtn.setVisible(false); saveInfoBtn.setManaged(false); }
    }
    private void setPageVisibility(boolean basic, boolean journal, boolean achievement) {
        if (basicInfoScroll != null) { basicInfoScroll.setVisible(basic); basicInfoScroll.setManaged(basic); }
        if (journalsPage != null) { journalsPage.setVisible(journal); journalsPage.setManaged(journal); }
        if (achievementsPage != null) { achievementsPage.setVisible(achievement); achievementsPage.setManaged(achievement); }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void goBack() {
        // Stop real-time updates before leaving
        stopRealTimeUpdates();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController dc = loader.getController();
            dc.setUser(soulId, soulId);
            if (root.getScene() != null) root.getScene().setRoot(dash);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
