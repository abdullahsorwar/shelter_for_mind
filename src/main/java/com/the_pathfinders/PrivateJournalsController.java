package com.the_pathfinders;

import com.the_pathfinders.db.JournalRepository;
import javafx.animation.ScaleTransition;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import com.the_pathfinders.util.JournalUtils;

import java.net.URL;
import java.util.List;

public class PrivateJournalsController {

    @FXML private VBox root;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox journalsContainer;
    @FXML private Button backBtn;

    private String currentSoulId = "";
    private JournalRepository journalRepo;

    public void setSoulId(String id) { this.currentSoulId = id == null ? "" : id; }

    @FXML
    public void initialize() {
        journalRepo = new JournalRepository();

        if (backBtn != null) backBtn.setOnAction(e -> goBackToDashboard());

        loadJournals();
    }

    private void loadJournals() {
        new Thread(() -> {
            try {
                List<Journal> journals = journalRepo.getJournalsBySoulId(currentSoulId);
                Platform.runLater(() -> {
                    journalsContainer.getChildren().clear();
                    
                    for (Journal journal : journals) {
                        VBox journalBox = createJournalBox(journal);
                        journalsContainer.getChildren().add(journalBox);
                        
                        // Smooth bump animation
                        journalBox.setScaleX(0);
                        journalBox.setScaleY(0);
                        ScaleTransition bump = new ScaleTransition(Duration.seconds(0.8), journalBox);
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
        // Outer VBox (solid gray) - same as public journals
        VBox outerBox = new VBox(10);
        outerBox.getStyleClass().add("journal-outer-box");
        outerBox.setPadding(new Insets(15));
        VBox.setMargin(outerBox, new Insets(10, 10, 10, 10));

        // Inner VBox (glassy)
        VBox innerBox = new VBox(10);
        innerBox.getStyleClass().add("journal-inner-box");
        innerBox.setPadding(new Insets(15));

        // Top section: User info with Edit button
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
        usernameLabel.setStyle("-fx-font-weight: bold;");
        
        Label timeLabel = new Label(JournalUtils.getRelativeTime(journal.getCreatedAt()));
        timeLabel.getStyleClass().add("journal-time");

        userTextBox.getChildren().addAll(usernameLabel, timeLabel);
        
        // Spacer to push edit button to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Edit button (top-right corner)
        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("edit-journal-btn");
        editBtn.setOnAction(e -> openJournalForEditing(journal.getId()));
        
        userInfoBox.getChildren().addAll(userIcon, userTextBox, spacer, editBtn);

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

        // Love section (same as public journals)
        HBox loveBox = new HBox(8);
        loveBox.setAlignment(Pos.CENTER_LEFT);

        Button loveBtn = new Button();
        loveBtn.getStyleClass().add("love-button");
        loveBtn.setBackground(javafx.scene.layout.Background.EMPTY);
        loveBtn.setBorder(javafx.scene.layout.Border.EMPTY);
        loveBtn.setPadding(javafx.geometry.Insets.EMPTY);
        loveBtn.setFocusTraversable(false);
        
        // Create ImageViews for heart icons
        ImageView heartOutline = JournalUtils.createHeartIcon("/assets/icons/heart_outline.png", 24);
        ImageView heartFilled = JournalUtils.createHeartIcon("/assets/icons/heart_filled.png", 24);
        
        loveBtn.setGraphic(heartOutline);
        
        Label loveCountLabel = new Label(String.valueOf(journal.getLoveCount() == null ? 0 : journal.getLoveCount()));
        loveCountLabel.getStyleClass().add("love-count");

        // Set initial loved state
        JournalUtils.setInitialLoveState(journal.getId(), currentSoulId, loveBtn, heartOutline, heartFilled);

        // Love button action
        loveBtn.setOnAction(e -> JournalUtils.toggleLove(journal.getId(), currentSoulId, loveBtn, loveCountLabel, journal, heartOutline, heartFilled));

        loveBox.getChildren().addAll(loveBtn, loveCountLabel);

        // Assemble inner box (WITH love section now)
        innerBox.getChildren().addAll(userInfoBox, separator, journalText, loveBox);
        outerBox.getChildren().add(innerBox);

        return outerBox;
    }

    private void openJournalForEditing(String journalId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/Journal.fxml"));
            Parent journRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JournalController jc) {
                jc.setSoulId(this.currentSoulId);
                jc.loadJournal(journalId);
            }
            if (root != null && root.getScene() != null) {
                root.getScene().setRoot(journRoot);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/the_pathfinders/fxml/dashboard.fxml"));
            Parent dash = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(this.currentSoulId, "");
            if (root != null && root.getScene() != null) root.getScene().setRoot(dash);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
