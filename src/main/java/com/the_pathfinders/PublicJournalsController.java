package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.the_pathfinders.util.JournalUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class PublicJournalsController {

    @FXML private VBox root;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox journalsContainer;
    @FXML private Button backBtn;

    private String currentSoulId = "";
    private JournalRepository journalRepo;
    private SavedJournalsManager savedJournalsManager;
    private Timeline timestampUpdateTimeline;
    private Timeline loveCountUpdateTimeline;
    private Timeline newJournalCheckTimeline;
    private String latestJournalId = null; // Track the most recent journal ID

    public void setSoulId(String id) {
        this.currentSoulId = id == null ? "" : id;
    }

    @FXML
    public void initialize() {
        journalRepo = new JournalRepository();
        savedJournalsManager = new SavedJournalsManager();

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToJournal());
        }

        // Load journals in background
        loadJournals();
        
        // Start timeline for real-time timestamp updates (every second)
        startTimestampUpdateTimeline();
        
        // Start timeline for real-time love count updates (every 2 seconds)
        startLoveCountUpdateTimeline();
        
        // Start timeline for checking new journals (every 3 seconds)
        startNewJournalCheckTimeline();
    }

    private void loadJournals() {
        new Thread(() -> {
            try {
                List<Journal> journals = journalRepo.getAllPublicJournals();

                Platform.runLater(() -> {
                    journalsContainer.getChildren().clear();
                    
                    // Track the latest journal ID (first in list since ordered DESC)
                    if (!journals.isEmpty()) {
                        latestJournalId = journals.get(0).getId();
                    }
                    
                    for (Journal journal : journals) {
                        VBox journalBox = createJournalBox(journal);
                        journalsContainer.getChildren().add(journalBox);
                        
                        // Smooth bump animation
                        journalBox.setScaleX(0);
                        journalBox.setScaleY(0);
                        ScaleTransition bump = new ScaleTransition(Duration.seconds(1), journalBox);
                        bump.setFromX(0);
                        bump.setFromY(0);
                        bump.setToX(1);
                        bump.setToY(1);
                        bump.setDelay(Duration.millis(journalsContainer.getChildren().indexOf(journalBox) * 50));
                        bump.play();
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load journals: " + ex.getMessage());
                    error.setStyle("-fx-text-fill: red;");
                    journalsContainer.getChildren().add(error);
                });
            }
        }).start();
    }

    private VBox createJournalBox(Journal journal) {
        // Outer VBox (solid gray)
        VBox outerBox = new VBox(10);
        outerBox.getStyleClass().add("journal-outer-box");
        outerBox.setPadding(new Insets(15));
        VBox.setMargin(outerBox, new Insets(10, 10, 10, 10));

        // Inner VBox (glassy)
        VBox innerBox = new VBox(10);
        innerBox.getStyleClass().add("journal-inner-box");
        innerBox.setPadding(new Insets(15));

        // Top section: User info
        HBox userInfoBox = new HBox(10);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);

        // User icon
        ImageView userIcon = new ImageView();
        try {
            URL iconUrl = getClass().getResource("/assets/icons/user.png");
            if (iconUrl != null) {
                Image img = new Image(iconUrl.toExternalForm(), 40, 40, true, true);
                userIcon.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Failed to load user icon: " + e.getMessage());
        }
        userIcon.setFitWidth(40);
        userIcon.setFitHeight(40);
        userIcon.setPreserveRatio(true);
        
        // Clip to circle
        Rectangle clip = new Rectangle(40, 40);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        userIcon.setClip(clip);

        // Username and time
        VBox userTextBox = new VBox(2);
        Label usernameLabel = new Label(journal.getSoulId());
        usernameLabel.getStyleClass().add("journal-username");
        usernameLabel.setStyle("-fx-font-weight: bold;"); // Bold username
        
        Label timeLabel = new Label(JournalUtils.getRelativeTime(journal.getCreatedAt()));
        timeLabel.getStyleClass().add("journal-time");
        timeLabel.setUserData(journal.getCreatedAt()); // Store timestamp for updates

        userTextBox.getChildren().addAll(usernameLabel, timeLabel);
        userInfoBox.getChildren().addAll(userIcon, userTextBox);

        // Gray separator line
        Region separator = new Region();
        separator.getStyleClass().add("journal-separator");
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);

        // Journal text - apply saved font
        Label journalText = new Label(journal.getText());
        journalText.setWrapText(true);
        journalText.getStyleClass().add("journal-text");
        
        // Apply saved font family and size
        String fontFamily = journal.getFontFamily() != null ? journal.getFontFamily() : "System";
        Integer fontSize = journal.getFontSize() != null ? journal.getFontSize() : 14;
        journalText.setStyle(String.format("-fx-font-family: '%s'; -fx-font-size: %dpx;", fontFamily, fontSize));

        // Love section
        HBox loveBox = new HBox(8);
        loveBox.setAlignment(Pos.CENTER_LEFT);

        Button loveBtn = new Button();
        loveBtn.getStyleClass().add("love-button");
        // Ensure no border/background even if CSS isn't loaded
        loveBtn.setBackground(javafx.scene.layout.Background.EMPTY);
        loveBtn.setBorder(javafx.scene.layout.Border.EMPTY);
        loveBtn.setPadding(javafx.geometry.Insets.EMPTY);
        loveBtn.setFocusTraversable(false); // Prevent scroll jumping
        
        // Create ImageViews for heart icons
        ImageView heartOutline = JournalUtils.createHeartIcon("/assets/icons/heart_outline.png",24);
        ImageView heartFilled = JournalUtils.createHeartIcon("/assets/icons/heart_filled.png",24);
        
        // Initial placeholder graphic
        loveBtn.setGraphic(heartOutline);
        
        Label loveCountLabel = new Label(String.valueOf(journal.getLoveCount() == null ? 0 : journal.getLoveCount()));
        loveCountLabel.getStyleClass().add("love-count");
        loveCountLabel.setUserData(journal.getId()); // Store journal ID for real-time updates

        // Set initial loved state using JournalUtils
        JournalUtils.setInitialLoveState(journal.getId(), currentSoulId, loveBtn, heartOutline, heartFilled);

        // Love button action using shared toggle
        loveBtn.setOnAction(e -> JournalUtils.toggleLove(journal.getId(), currentSoulId, loveBtn, loveCountLabel, journal, heartOutline, heartFilled));

        // Add spacer to push save button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Save for Later button (matching BlogDetailController style with green background and text)
        Button saveBtn = new Button();
        saveBtn.setFocusTraversable(false);
        
        // Set initial state
        boolean isSaved = savedJournalsManager.isJournalSaved(currentSoulId, journal.getId());
        journal.setSavedForLater(isSaved);
        
        if (isSaved) {
            saveBtn.setText("★ Saved");
            saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-radius: 5;");
        } else {
            saveBtn.setText("☆ Save for Later");
            saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-radius: 5;");
        }
        
        // Save button action with smooth transition
        saveBtn.setOnAction(e -> {
            boolean currentlySaved = journal.isSavedForLater();
            
            // Create fade transition
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), saveBtn);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            
            fade.setOnFinished(evt -> {
                if (currentlySaved) {
                    // Unsave
                    savedJournalsManager.removeSavedJournal(currentSoulId, journal.getId());
                    journal.setSavedForLater(false);
                    saveBtn.setText("☆ Save for Later");
                    saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-radius: 5;");
                } else {
                    // Save
                    savedJournalsManager.saveJournal(currentSoulId, journal);
                    journal.setSavedForLater(true);
                    saveBtn.setText("★ Saved");
                    saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-radius: 5;");
                }
                
                // Fade back in
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), saveBtn);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
            fade.play();
        });

        loveBox.getChildren().addAll(loveBtn, loveCountLabel, spacer, saveBtn);

        // Assemble inner box
        innerBox.getChildren().addAll(userInfoBox, separator, journalText, loveBox);
        outerBox.getChildren().add(innerBox);

        return outerBox;
    }

    
    private void startTimestampUpdateTimeline() {
        // Create a timeline that updates all timestamps every second
        timestampUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateAllTimestamps();
        }));
        timestampUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        timestampUpdateTimeline.play();
    }
    
    private void updateAllTimestamps() {
        // Iterate through all journal boxes and update time labels
        for (javafx.scene.Node outerNode : journalsContainer.getChildren()) {
            if (outerNode instanceof VBox outerBox) {
                for (javafx.scene.Node innerNode : outerBox.getChildren()) {
                    if (innerNode instanceof VBox innerBox) {
                        for (javafx.scene.Node child : innerBox.getChildren()) {
                            if (child instanceof HBox hbox) {
                                for (javafx.scene.Node hboxChild : hbox.getChildren()) {
                                    if (hboxChild instanceof VBox userTextBox) {
                                        for (javafx.scene.Node textChild : userTextBox.getChildren()) {
                                            if (textChild instanceof Label label && label.getStyleClass().contains("journal-time")) {
                                                // Get the journal object from user data
                                                LocalDateTime timestamp = (LocalDateTime) label.getUserData();
                                                if (timestamp != null) {
                                                        label.setText(JournalUtils.getRelativeTime(timestamp));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void startLoveCountUpdateTimeline() {
        // Create a timeline that syncs love counts from database every 2 seconds
        loveCountUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            updateAllLoveCounts();
        }));
        loveCountUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        loveCountUpdateTimeline.play();
    }
    
    private void updateAllLoveCounts() {
        // Run database queries in background thread
        new Thread(() -> {
            try {
                // Iterate through all journal boxes and update love counts
                for (javafx.scene.Node outerNode : journalsContainer.getChildren()) {
                    if (outerNode instanceof VBox outerBox) {
                        for (javafx.scene.Node innerNode : outerBox.getChildren()) {
                            if (innerNode instanceof VBox innerBox) {
                                for (javafx.scene.Node child : innerBox.getChildren()) {
                                    if (child instanceof HBox loveBox && !loveBox.getChildren().isEmpty()) {
                                        // Check if this is the love box (has button and label)
                                        for (javafx.scene.Node loveChild : loveBox.getChildren()) {
                                            if (loveChild instanceof Label loveCountLabel && 
                                                loveCountLabel.getStyleClass().contains("love-count")) {
                                                
                                                String journalId = (String) loveCountLabel.getUserData();
                                                if (journalId != null) {
                                                    try {
                                                        // Fetch latest count from database
                                                        int latestCount = journalRepo.getLoveCount(journalId);
                                                        
                                                        // Update UI on JavaFX thread
                                                        Platform.runLater(() -> {
                                                            String currentText = loveCountLabel.getText();
                                                            int currentCount = 0;
                                                            try {
                                                                currentCount = Integer.parseInt(currentText);
                                                            } catch (NumberFormatException ignored) {}
                                                            
                                                            // Only update if count changed (avoid unnecessary UI updates)
                                                            if (currentCount != latestCount) {
                                                                loveCountLabel.setText(String.valueOf(latestCount));
                                                            }
                                                        });
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // Silently handle errors to avoid disrupting the timeline
        }
    }).start();
}

private void startNewJournalCheckTimeline() {
    // Create a timeline that checks for new journals every 3 seconds
    newJournalCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
        checkForNewJournals();
    }));
    newJournalCheckTimeline.setCycleCount(Animation.INDEFINITE);
    newJournalCheckTimeline.play();
}

private void checkForNewJournals() {
    // Skip check if we don't have a reference point yet
    if (latestJournalId == null) {
        return;
    }
    
    // Run database query in background thread
    new Thread(() -> {
        try {
            // Get journals newer than the current latest
            List<Journal> newJournals = journalRepo.getNewJournalsSince(latestJournalId);
            
            if (!newJournals.isEmpty()) {
                Platform.runLater(() -> {
                    // Add new journals to the top of the list (reverse order since we got DESC from DB)
                    for (int i = newJournals.size() - 1; i >= 0; i--) {
                        Journal journal = newJournals.get(i);
                        VBox journalBox = createJournalBox(journal);
                        
                        // Add to the beginning of the container
                        journalsContainer.getChildren().add(0, journalBox);
                        
                        // Smooth bump animation for new journals
                        journalBox.setScaleX(0);
                        journalBox.setScaleY(0);
                        ScaleTransition bump = new ScaleTransition(Duration.seconds(0.5), journalBox);
                        bump.setFromX(0);
                        bump.setFromY(0);
                        bump.setToX(1);
                        bump.setToY(1);
                        bump.play();
                        
                        // Update latest journal ID to the newest one
                        if (i == 0) {
                            latestJournalId = journal.getId();
                        }
                    }
                    
                    System.out.println("Added " + newJournals.size() + " new journal(s) in real-time!");
                });
            }
        } catch (Exception ex) {
            // Silently handle errors to avoid disrupting the timeline
            System.err.println("Error checking for new journals: " + ex.getMessage());
        }
    }).start();
}

    private void goBackToJournal() {
        // Stop all timelines when leaving the view
        if (timestampUpdateTimeline != null) {
            timestampUpdateTimeline.stop();
        }
        if (loveCountUpdateTimeline != null) {
            loveCountUpdateTimeline.stop();
        }
        if (newJournalCheckTimeline != null) {
            newJournalCheckTimeline.stop();
        }
        
        // Go back to Dashboard instead of Journal
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dashRoot = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController dc) {
                dc.setUser(this.currentSoulId, "");
            }

            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(dashRoot);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
